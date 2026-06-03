package com.email.platform;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * DBAgent 兼容层 —— 完全对齐 OA com.seeyon.ctp.util.DBAgent 的方法签名。
 *
 * <p>底层使用 Hibernate SessionFactory，所有静态方法签与 OA 一致，
 * 确保 InternalMailManagerImpl / InternalMailDaoImpl 中的 DBAgent 调用零改动。</p>
 */
public class DBAgent {

    private static SessionFactory sessionFactory;

    public static void setSessionFactory(SessionFactory sf) {
        sessionFactory = sf;
    }

    /** 供 JDBCAgent 取 SessionFactory */
    static SessionFactory getSessionFactorySnapshot() {
        return sessionFactory;
    }

    private static Session getSession() {
        if (sessionFactory == null) {
            throw new IllegalStateException("SessionFactory 未初始化");
        }
        // 优先从 Spring 事务获取当前 Session，否则打开新 Session
        try {
            return sessionFactory.getCurrentSession();
        } catch (Exception e) {
            return sessionFactory.openSession();
        }
    }

    // ==================== find ====================

    /**
     * HQL 查询（不分页）
     */
    @SuppressWarnings("rawtypes")
    public static List find(String hql, Map<String, Object> params) {
        Query<?> q = getSession().createQuery(hql);
        applyParams(q, params);
        return q.list();
    }

    /**
     * HQL 查询（分页，对齐 OA DBAgent.find(hql, params, flipInfo)）
     * <p>内部处理分页和 count，结果写入 flipInfo.data 和 flipInfo.total。</p>
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List find(String hql, Map<String, Object> params, FlipInfo fi) {
        Query q = getSession().createQuery(hql);
        applyParams(q, params);

        // count
        if (fi.isNeedTotal()) {
            String countHql = buildCountHql(hql);
            Query<?> cq = getSession().createQuery(countHql);
            applyParams(cq, params);
            fi.setTotal(((Number) cq.uniqueResult()).longValue());
        }

        // 分页
        if (fi.getPage() > 0 && fi.getSize() > 0) {
            q.setFirstResult((fi.getPage() - 1) * fi.getSize());
            q.setMaxResults(fi.getSize());
        }

        List result = q.list();
        fi.setData(result);
        return result;
    }

    // ==================== get ====================

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz, Serializable id) {
        return getSession().get(clazz, id);
    }

    // ==================== save / update / delete ====================

    public static void save(Object entity) {
        Session s = getSession();
        Transaction tx = null;
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            tx = s.beginTransaction();
        }
        try {
            s.save(entity);
            if (tx != null) tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public static void update(Object entity) {
        Session s = getSession();
        Transaction tx = null;
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            tx = s.beginTransaction();
        }
        try {
            s.update(entity);
            if (tx != null) tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public static void delete(Object entity) {
        Session s = getSession();
        Transaction tx = null;
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            tx = s.beginTransaction();
        }
        try {
            s.delete(entity);
            if (tx != null) tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public static void saveAll(Collection<?> entities) {
        Session s = getSession();
        Transaction tx = null;
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            tx = s.beginTransaction();
        }
        try {
            int i = 0;
            for (Object e : entities) {
                s.save(e);
                if (++i % 20 == 0) { s.flush(); s.clear(); }
            }
            if (tx != null) tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public static void updateAll(Collection<?> entities) {
        Session s = getSession();
        Transaction tx = null;
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            tx = s.beginTransaction();
        }
        try {
            int i = 0;
            for (Object e : entities) {
                s.update(e);
                if (++i % 20 == 0) { s.flush(); s.clear(); }
            }
            if (tx != null) tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public static void deleteAll(Collection<?> entities) {
        Session s = getSession();
        Transaction tx = null;
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            tx = s.beginTransaction();
        }
        try {
            for (Object e : entities) {
                s.delete(e);
            }
            if (tx != null) tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    // ==================== 命名查询 ====================

    @SuppressWarnings("rawtypes")
    public static List findByNamedQuery(String queryName, Map<String, Object> params) {
        Query q = getSession().getNamedQuery(queryName);
        applyParams(q, params);
        return q.list();
    }

    // ==================== count / bulkUpdate ====================

    public static long count(String hql, Map<String, Object> params) {
        String countHql = buildCountHql(hql);
        Query<?> q = getSession().createQuery(countHql);
        applyParams(q, params);
        return ((Number) q.uniqueResult()).longValue();
    }

    public static int bulkUpdate(String hql, Map<String, Object> params) {
        Query<?> q = getSession().createQuery(hql);
        applyParams(q, params);
        return q.executeUpdate();
    }

    /** OA DBAgent.bulkUpdate(hql, positionalParams...) — 兼容位置参数 */
    public static int bulkUpdate(String hql, Object... positionalParams) {
        Query<?> q = getSession().createQuery(hql);
        if (positionalParams != null) {
            int i = 0;
            for (Object p : positionalParams) {
                q.setParameter(i++, p);
            }
        }
        return q.executeUpdate();
    }

    // ==================== 内部工具 ====================

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void applyParams(Query q, Map<String, Object> params) {
        if (params != null) {
            for (Map.Entry<String, Object> e : params.entrySet()) {
                q.setParameter(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 把 HQL 转为 count 查询
     */
    private static String buildCountHql(String hql) {
        String upper = hql.toUpperCase().trim();
        String result = hql;
        // SELECT ... FROM → SELECT COUNT(*) FROM
        if (upper.startsWith("SELECT")) {
            int fromIdx = upper.indexOf(" FROM ");
            if (fromIdx > 0) {
                result = hql.substring(fromIdx);
            }
        }
        // 如果还是以 FROM 开头，去掉前导空白
        result = result.trim();
        // 去掉 ORDER BY
        int orderIdx = result.toUpperCase().lastIndexOf("ORDER BY");
        if (orderIdx > 0) {
            result = result.substring(0, orderIdx);
        }
        // 去掉 FETCH（如果存在）
        return "SELECT COUNT(*) " + result;
    }
}
