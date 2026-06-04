package com.email.platform;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** DBAgent兼容层 - 对齐OA签名 */
public class DBAgent {

    private static SessionFactory sessionFactory;
    public static void setSessionFactory(SessionFactory sf) { sessionFactory = sf; }
    static SessionFactory getSessionFactorySnapshot() { return sessionFactory; }

    private static Session getSession() {
        if (sessionFactory == null) throw new IllegalStateException("SessionFactory未初始化");
        try { return sessionFactory.getCurrentSession(); }
        catch (Exception e) { return sessionFactory.openSession(); }
    }

    @SuppressWarnings("rawtypes")
    public static List find(String hql, Map<String, Object> params) {
        Query<?> q = getSession().createQuery(hql);
        applyParams(q, params);
        return q.list();
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    public static List find(String hql, Map<String, Object> params, FlipInfo fi) {
        Query q = getSession().createQuery(hql);
        applyParams(q, params);
        if (fi.isNeedTotal()) {
            Query<?> cq = getSession().createQuery(buildCountHql(hql));
            applyParams(cq, params);
            fi.setTotal(((Number) cq.uniqueResult()).longValue());
        }
        if (fi.getPage() > 0 && fi.getSize() > 0) {
            q.setFirstResult((fi.getPage() - 1) * fi.getSize());
            q.setMaxResults(fi.getSize());
        }
        List result = q.list();
        fi.setData(result);
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz, Serializable id) { return getSession().get(clazz, id); }

    private static void runWithTx(Session s, Runnable action) {
        Transaction tx = null;
        if (!TransactionSynchronizationManager.isActualTransactionActive()) tx = s.beginTransaction();
        try { action.run(); if (tx != null) tx.commit(); }
        catch (Exception e) { if (tx != null) tx.rollback(); throw new RuntimeException(e); }
    }

    public static void save(Object entity) { runWithTx(getSession(), () -> getSession().save(entity)); }
    public static void update(Object entity) { runWithTx(getSession(), () -> { getSession().saveOrUpdate(entity); getSession().flush(); }); }
    public static void delete(Object entity) { runWithTx(getSession(), () -> getSession().delete(entity)); }
    public static void saveAll(Collection<?> entities) { runWithTx(getSession(), () -> { Session s = getSession(); int i = 0; for (Object e : entities) { s.save(e); if (++i % 20 == 0) { s.flush(); s.clear(); } } }); }
    public static void updateAll(Collection<?> entities) { runWithTx(getSession(), () -> { Session s = getSession(); int i = 0; for (Object e : entities) { s.update(e); if (++i % 20 == 0) { s.flush(); s.clear(); } } }); }
    public static void deleteAll(Collection<?> entities) { runWithTx(getSession(), () -> entities.forEach(getSession()::delete)); }

    @SuppressWarnings("rawtypes")
    public static List findByNamedQuery(String name, Map<String, Object> params) { Query q = getSession().getNamedQuery(name); applyParams(q, params); return q.list(); }

    public static long count(String hql, Map<String, Object> params) { Query<?> q = getSession().createQuery(buildCountHql(hql)); applyParams(q, params); return ((Number) q.uniqueResult()).longValue(); }

    public static int bulkUpdate(String hql, Map<String, Object> params) { Session s = getSession(); Transaction tx = null; if (!TransactionSynchronizationManager.isActualTransactionActive()) tx = s.beginTransaction(); try { Query<?> q = s.createQuery(hql); applyParams(q, params); int r = q.executeUpdate(); if (tx != null) tx.commit(); return r; } catch (Exception e) { if (tx != null) tx.rollback(); throw new RuntimeException(e); } }

    public static int bulkUpdate(String hql, Object... positionalParams) { Session s = getSession(); Transaction tx = null; if (!TransactionSynchronizationManager.isActualTransactionActive()) tx = s.beginTransaction(); try { Query<?> q = s.createQuery(hql); if (positionalParams != null) for (int i = 0; i < positionalParams.length; i++) q.setParameter(i, positionalParams[i]); int r = q.executeUpdate(); if (tx != null) tx.commit(); return r; } catch (Exception e) { if (tx != null) tx.rollback(); throw new RuntimeException(e); } }

    @SuppressWarnings({"rawtypes","unchecked"})
    private static void applyParams(Query q, Map<String, Object> params) { if (params != null) params.forEach(q::setParameter); }

    private static String buildCountHql(String hql) {
        String upper = hql.toUpperCase().trim(), result = hql;
        if (upper.startsWith("SELECT")) { int fi = upper.indexOf(" FROM "); if (fi > 0) result = hql.substring(fi); }
        result = result.trim(); int oi = result.toUpperCase().lastIndexOf("ORDER BY"); if (oi > 0) result = result.substring(0, oi);
        return "SELECT COUNT(*) " + result;
    }
}
