package com.email.dao;

import com.email.constants.InMailConstant;
import com.email.entity.*;
import com.email.platform.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * 邮件核心 DAO —— HQL 查询从 OA InternalMailDaoImpl 原样复制。
 * 去掉跨网/审批/JDBC等独立部署不需要的功能，保留核心收发查询。
 */
public class InternalMailDao {

    private static final Log log = LogFactory.getLog(InternalMailDao.class);

    /** 收件箱 — HQL 从 OA 原样复制 */
    @SuppressWarnings("unchecked")
    public List<InMailAffair> findInboxAffair(FlipInfo fi, Map<String, Object> param, Long memberId, int state) {
        return DBAgent.find(buildInboxHql(fi, param), buildInboxParams(param, memberId, state), fi);
    }

    /** 发件箱(含已发送和收件箱) — HQL 从 OA 原样复制 */
    @SuppressWarnings("unchecked")
    public List<InMailAffair> findSentAffair(FlipInfo fi, Map<String, Object> param, Long memberId, int state) {
        return DBAgent.find(buildSentHql(fi, param), buildSentParams(param, memberId, state), fi);
    }

    /** 草稿箱 */
    @SuppressWarnings("unchecked")
    public List<InMailAffair> findDraftAffair(FlipInfo fi, Map<String, Object> param, Long memberId, int state) {
        return DBAgent.find(buildDraftHql(fi, param), buildDraftParams(param, memberId, state), fi);
    }

    /** 已删除 */
    @SuppressWarnings("unchecked")
    public List<InMailAffair> findDeleteAffair(FlipInfo fi, Map<String, Object> param, Long memberId, int state) {
        return DBAgent.find(buildDeleteHql(fi, param), buildDeleteParams(param, memberId, state), fi);
    }

    /** 按ID查 */
    public InMailSummary getInMailSummaryById(Long id) { return DBAgent.get(InMailSummary.class, id); }
    public InMailAffair getInMailAffairById(Long id) { return DBAgent.get(InMailAffair.class, id); }

    /** 按 SummaryId 查子表 */
    @SuppressWarnings("unchecked")
    public List<InMailAffair> getInMailAffairByObjectId(Long objectId) {
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", objectId);
        return DBAgent.find("FROM InMailAffair WHERE objectId=:objectId and (STATE>0 or STATE<0) order by CREATE_DATE desc", params);
    }

    /** 批量查 */
    public List<InMailAffair> getInMailAffairsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        Map<String, Object> params = new HashMap<>();
        params.put("ids", ids);
        return DBAgent.find("FROM InMailAffair WHERE id IN (:ids)", params);
    }

    /** 收件箱数量 */
    public int findInboxNextAffair(Map<String, Object> param) {
        Long memberId = (Long) param.get("memberId");
        Map<String, Object> p = new HashMap<>(); p.put("mid", memberId);
        return (int) DBAgent.count("FROM InMailAffair WHERE memberId=:mid AND state=2 AND isDelete=0", p);
    }

    /** 获取登录用户邮件签名(从 org_member 查单位-部门-岗位-人员) */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getSearchDataStr(String key, String isS) {
        Map<String, Object> p = new HashMap<>();
        p.put("kw", "%" + key + "%");
        return DBAgent.find("FROM OrgMember WHERE name LIKE :kw OR loginName LIKE :kw AND enabled=1", p);
    }

    // ===== HQL 构建 (从 OA InternalMailDaoImpl 原样提取) =====

    private String buildInboxHql(FlipInfo fi, Map<String, Object> param) {
        StringBuilder hql = new StringBuilder("SELECT m FROM InMailSummary p, InMailAffair m");
        hql.append(" WHERE p.id=m.objectId");
        appendCommonWhere(hql, param);
        hql.append(" AND m.state IN (0,2)");
        hql.append(" AND m.isDelete=0");
        return hql.toString();
    }

    private Map<String, Object> buildInboxParams(Map<String, Object> param, Long memberId, int state) {
        Map<String, Object> p = new HashMap<>(); p.put("mid", memberId);
        appendCommonParams(p, param);
        return p;
    }

    private String buildSentHql(FlipInfo fi, Map<String, Object> param) {
        return "FROM InMailAffair WHERE senderId=:sid AND state=0 AND isDelete=0 AND memberId=:sid";
    }
    private Map<String, Object> buildSentParams(Map<String, Object> param, Long memberId, int state) {
        Map<String, Object> p = new HashMap<>(); p.put("sid", memberId); return p;
    }

    private String buildDraftHql(FlipInfo fi, Map<String, Object> param) {
        return "FROM InMailAffair WHERE senderId=:sid AND state=1 AND isDelete=0 AND memberId=:sid";
    }
    private Map<String, Object> buildDraftParams(Map<String, Object> param, Long memberId, int state) {
        Map<String, Object> p = new HashMap<>(); p.put("sid", memberId); return p;
    }

    private String buildDeleteHql(FlipInfo fi, Map<String, Object> param) {
        return "FROM InMailAffair WHERE memberId=:mid AND state=3 AND isDelete=0";
    }
    private Map<String, Object> buildDeleteParams(Map<String, Object> param, Long memberId, int state) {
        Map<String, Object> p = new HashMap<>(); p.put("mid", memberId); return p;
    }

    private void appendCommonWhere(StringBuilder hql, Map<String, Object> param) {
        hql.append(" AND m.memberId=:mid");
        if (param.get("readType") != null) {
            String rt = (String) param.get("readType");
            if ("notRead".equals(rt)) hql.append(" AND m.readFlag=0");
            else if ("Handled".equals(rt)) hql.append(" AND m.isHandled=1");
            else if ("NotHandled".equals(rt)) hql.append(" AND m.isHandled=0");
        }
        if (param.get("subject") != null) hql.append(" AND p.subject LIKE :subject");
        if (param.get("senderName") != null) hql.append(" AND p.startMemberId=s.id AND s.name LIKE :senderName");
        if (param.get("contentName") != null) hql.append(" AND p.content LIKE :contentName");
        if (param.get("path") != null) hql.append(" AND m.path=:path");
    }

    private void appendCommonParams(Map<String, Object> p, Map<String, Object> param) {
        if (param.get("subject") != null) p.put("subject", "%" + param.get("subject") + "%");
        if (param.get("senderName") != null) p.put("senderName", "%" + param.get("senderName") + "%");
        if (param.get("contentName") != null) p.put("contentName", "%" + param.get("contentName") + "%");
        if (param.get("path") != null) p.put("path", param.get("path"));
    }
}
