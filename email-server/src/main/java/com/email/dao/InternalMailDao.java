package com.email.dao;

import com.email.constants.InMailConstant;
import com.email.entity.*;
import com.email.platform.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * 邮件核心 DAO —— HQL 从 OA InternalMailDaoImpl 原样提取。
 */
@Repository
public class InternalMailDao {

    private static final Log log = LogFactory.getLog(InternalMailDao.class);

    // ===== 基础查询 =====

    public InMailSummary getInMailSummaryById(Long id) {
        return DBAgent.get(InMailSummary.class, id);
    }

    public InMailAffair getInMailAffairById(Long id) {
        return DBAgent.get(InMailAffair.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<InMailAffair> getInMailAffairsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        Map<String, Object> p = new HashMap<>();
        p.put("ids", ids);
        return DBAgent.find("FROM InMailAffair WHERE id IN (:ids)", p);
    }

    @SuppressWarnings("unchecked")
    public List<InMailAffair> getInMailAffairByObjectId(Long objectId) {
        Map<String, Object> p = new HashMap<>();
        p.put("objectId", objectId);
        return DBAgent.find("FROM InMailAffair WHERE objectId=:objectId AND (STATE>0 OR STATE<0) ORDER BY CREATE_DATE DESC", p);
    }

    /** 收件箱 */
    @SuppressWarnings("unchecked")
    public void findInboxAffair(FlipInfo fi, Map<String, Object> param, Long memberId, int state) {
        StringBuilder hql = new StringBuilder("FROM InMailAffair m ");
        Map<String, Object> p = new HashMap<>();

        // OA 原样 HQL: FROM InMailSummary p, InMailAffair m WHERE p.id=m.objectId
        boolean hasJoin = false;
        if (param.get("subject") != null || param.get("contentName") != null || param.get("attachName") != null) {
            hql.insert(0, "SELECT m FROM InMailSummary p, InMailAffair m ");
            hasJoin = true;
        }
        appendWhere(hql, param, memberId, state, hasJoin, p);
        DBAgent.find(hql.toString(), p, fi);
    }

    /** 发件箱 */
    @SuppressWarnings("unchecked")
    public void findSentAffair(FlipInfo fi, Map<String, Object> param, Long memberId, int state) {
        String hql = "FROM InMailAffair WHERE senderId=:sid AND state=0 AND delFlag=0 AND memberId=:sid";
        Map<String, Object> p = new HashMap<>(); p.put("sid", memberId);
        DBAgent.find(hql, p, fi);
    }

    /** 草稿箱 */
    @SuppressWarnings("unchecked")
    public void findDraftAffair(FlipInfo fi, Map<String, Object> param, Long memberId, int state) {
        String hql = "FROM InMailAffair WHERE senderId=:sid AND state=1 AND delFlag=0 AND memberId=:sid ORDER BY updateDate DESC";
        Map<String, Object> p = new HashMap<>(); p.put("sid", memberId);
        DBAgent.find(hql, p, fi);
    }

    /** 已删除 */
    @SuppressWarnings("unchecked")
    public void findDeleteAffair(FlipInfo fi, Map<String, Object> param, Long memberId, int state) {
        String hql = "FROM InMailAffair WHERE memberId=:mid AND state=3 AND delFlag=0 ORDER BY updateDate DESC";
        Map<String, Object> p = new HashMap<>(); p.put("mid", memberId);
        DBAgent.find(hql, p, fi);
    }

    /** 收件箱未读/已办数量 */
    public int findInboxNextAffair(Map<String, Object> param) {
        Long memberId = (Long) param.get("memberId");
        Map<String, Object> p = new HashMap<>(); p.put("mid", memberId);
        return (int) DBAgent.count("FROM InMailAffair WHERE memberId=:mid AND state=2 AND delFlag=0", p);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getSearchDataStr(String key, String isS) {
        Map<String, Object> p = new HashMap<>();
        p.put("kw", "%" + key + "%");
        return DBAgent.find("FROM OrgMember WHERE (name LIKE :kw OR loginName LIKE :kw) AND enabled=1", p);
    }

    // ===== HQL WHERE 构建 =====

    private void appendWhere(StringBuilder hql, Map<String, Object> param,
                              Long memberId, int state, boolean hasJoin, Map<String, Object> p) {
        if (hasJoin) {
            hql.append(" WHERE p.id=m.objectId");
        } else {
            hql.append(" WHERE ");
        }

        if (hasJoin) hql.append(" AND ");
        hql.append(" m.memberId=:mid AND m.state IN (0,2) AND m.delFlag=0");
        p.put("mid", memberId);

        String readType = (String) param.get("readType");
        if ("notRead".equals(readType)) hql.append(" AND m.readFlag=0");
        else if ("Handled".equals(readType)) hql.append(" AND m.isHandled=1");
        else if ("NotHandled".equals(readType)) hql.append(" AND m.isHandled=0");

        if (param.get("subject") != null) {
            hql.append(" AND p.subject LIKE :subject");
            p.put("subject", "%" + param.get("subject") + "%");
        }
        if (param.get("senderName") != null) {
            hql.append(" AND p.startMemberId=s.id AND s.name LIKE :senderName");
            p.put("senderName", "%" + param.get("senderName") + "%");
        }
        if (param.get("contentName") != null) {
            hql.append(" AND p.content LIKE :contentName");
            p.put("contentName", "%" + param.get("contentName") + "%");
        }
        if (param.get("attachName") != null) {
            hql.append(" AND p.id=a.subReference AND a.filename LIKE :attachName");
            p.put("attachName", "%" + param.get("attachName") + "%");
        }
        if (param.get("path") != null) {
            hql.append(" AND m.path=:path");
            p.put("path", param.get("path"));
        }
    }
}
