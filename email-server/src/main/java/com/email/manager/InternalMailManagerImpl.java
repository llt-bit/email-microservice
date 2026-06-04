package com.email.manager;

import com.email.constants.InMailConstant;
import com.email.dao.InternalMailDao;
import com.email.entity.*;
import com.email.platform.*;
import com.email.platform.entity.OrgMember;
import com.email.platform.entity.OrgUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 邮件核心业务实现 —— 从 OA InternalMailManagerImpl 提取核心逻辑。
 * 所有方法签名、业务逻辑与 OA 保持一致，只替换 DBAgent/AppContext 调用点。
 */
@Service("internalMailManager")  // bean 名对齐 OA
public class InternalMailManagerImpl implements InternalMailManager {

    private static final Log log = LogFactory.getLog(InternalMailManagerImpl.class);

    @Resource private InternalMailDao dao;

    // ==================== 查询 ====================

    @Override
    public InMailSummary getInMailSummaryById(long id) {
        return dao.getInMailSummaryById(id);
    }

    @Override
    public InMailAffair getInMailAffairById(long id) {
        return dao.getInMailAffairById(id);
    }

    @Override
    public List<InMailAffair> getInMailAffairsByIds(List<Long> ids) {
        return dao.getInMailAffairsByIds(ids);
    }

    /** 邮件查阅情况 — 从 OA assemblyData 原样复制逻辑 */
    @Override
    public List<InMailAffair> findAffairByInMailId(Long inMailId) {
        return dao.getInMailAffairByObjectId(inMailId);
    }

    @Override
    public List<InMailAffairBO> findAffairBOByInMailId(Long inMailId) {
        return assemblyData(findAffairByInMailId(inMailId));
    }

    // ==================== 列表 ====================

    @Override
    public FlipInfo findInboxAffair(FlipInfo fi, Long userId, Map<String, Object> param) {
        param.put("memberId", userId);
        dao.findInboxAffair(fi, param, userId, InMailConstant.InMailAffairState.run.ordinal());
        return fi;
    }

    @Override
    public FlipInfo findDraftAffair(FlipInfo fi, Long userId, Map<String, Object> param) {
        param.put("memberId", userId);
        dao.findDraftAffair(fi, param, userId, InMailConstant.InMailAffairState.draf.ordinal());
        return fi;
    }

    @Override
    public FlipInfo findSentAffair(FlipInfo fi, Long userId, Map<String, Object> param) {
        param.put("memberId", userId);
        dao.findSentAffair(fi, param, userId, InMailConstant.InMailAffairState.sent.ordinal());
        return fi;
    }

    @Override
    public FlipInfo findDeleteAffair(FlipInfo fi, Long userId, Map<String, Object> param) {
        param.put("memberId", userId);
        dao.findDeleteAffair(fi, param, userId, InMailConstant.InMailAffairState.deleted.ordinal());
        return fi;
    }

    @Override
    public FlipInfo findCollectAffair(FlipInfo fi, Long userId, Map<String, Object> param) {
        // 收藏：collect=1 且 state!=3(不在已删除中)
        Map<String, Object> p = new HashMap<>();
        p.put("mid", userId);
        DBAgent.find("FROM InMailAffair WHERE memberId=:mid AND collect=1 AND state!=3 AND delFlag=0", p, fi);
        return fi;
    }

    @Override
    public FlipInfo findInboxAffairBySecret(FlipInfo fi, Map<String, Object> param) {
        Map<String, Object> p = new HashMap<>();
        p.put("mid", param.get("userId"));
        DBAgent.find("FROM InMailAffair WHERE memberId=:mid AND state=5 AND delFlag=0", p, fi);
        return fi;
    }

    @Override
    public int findInboxNextAffair(Map<String, Object> param) {
        return dao.findInboxNextAffair(param);
    }

    // ==================== 保存 ====================

    @Override
    public void saveInMailSummary(InMailSummary summary, List<InMailAffair> list, boolean isExist) {
        if (isExist) {
            DBAgent.update(summary);
        } else {
            DBAgent.save(summary);
        }
        if (list != null && !list.isEmpty()) {
            for (InMailAffair a : list) {
                if (a.getId() != null && DBAgent.get(InMailAffair.class, a.getId()) != null) {
                    DBAgent.update(a);
                } else {
                    DBAgent.save(a);
                }
            }
        }
    }

    @Override
    public void updateInMailAffair(InMailAffair affair) {
        affair.setUpdateDate(new Date());
        DBAgent.update(affair);
    }

    @Override
    public void updateInMailAffairs(List<InMailAffair> list) {
        for (InMailAffair a : list) {
            a.setUpdateDate(new Date());
            DBAgent.update(a);
        }
    }

    // ==================== 标记操作 ====================

    @Override
    public boolean updateAllAffairIsHandle(List<Long> affairIds, String flagType) {
        if (affairIds == null || affairIds.isEmpty()) return false;
        List<InMailAffair> list = dao.getInMailAffairsByIds(affairIds);
        switch (flagType) {
            case "handled":
                for (InMailAffair a : list) { a.setIsHandled(true); a.setUpdateDate(new Date()); }
                break;
            case "collection":
                for (InMailAffair a : list) { a.setCollect(1); a.setUpdateDate(new Date()); }
                break;
            case "cancelCollection":
                for (InMailAffair a : list) { a.setCollect(0); a.setUpdateDate(new Date()); }
                break;
            case "encryption":
                for (InMailAffair a : list) { a.setState(5); a.setUpdateDate(new Date()); }
                break;
            case "cancelEncryption":
                for (InMailAffair a : list) { a.setState(2); a.setUpdateDate(new Date()); }
                break;
            default: return false;
        }
        updateInMailAffairs(list);
        return true;
    }

    // ==================== 删除/恢复 ====================

    @Override
    public void deleteAffair(String pageType, String affairId) {
        if (!Strings.isNotBlank(affairId) || !Strings.isNotBlank(pageType)) return;
        InMailAffair a = DBAgent.get(InMailAffair.class, Long.parseLong(affairId));
        if (a == null) return;
        if ("delete".equals(pageType)) {
            a.setDelete(true);
        } else {
            if ("sent".equals(pageType)) a.setDeleteState(0);
            else if ("draft".equals(pageType)) a.setDeleteState(1);
            else if ("inbox".equals(pageType) || "inBox".equals(pageType)) a.setDeleteState(2);
            else if ("collection".equals(pageType)) a.setDeleteState(a.getState());
            a.setState(InMailConstant.InMailAffairState.deleted.ordinal());
        }
        a.setUpdateDate(new Date());
        DBAgent.update(a);
    }

    @Override
    public void recoveryAffair(String pageType, String affairId) {
        if (!Strings.isNotBlank(affairId)) return;
        InMailAffair a = DBAgent.get(InMailAffair.class, Long.parseLong(affairId));
        if (a == null || a.getDeleteState() == null) return;
        a.setState(a.getDeleteState());
        a.setDeleteState(null);
        a.setUpdateDate(new Date());
        DBAgent.update(a);
    }

    @Override
    public boolean updateFieldById(String field, Object fieldVal, List<Long> ids, Long id) {
        if (ids == null && id != null) ids = Collections.singletonList(id);
        if (ids == null || ids.isEmpty()) return false;
        String sql = "UPDATE PRO_INMAIL_AFFAIR SET " + field + " = ? WHERE ID IN (" +
                ids.toString().replace("[", "").replace("]", "") + ")";
        try {
            JDBCAgent agent = new JDBCAgent();
            List<Object> params = new ArrayList<>();
            params.add(fieldVal);
            agent.execute(sql, params);
            agent.close();
            return true;
        } catch (Exception e) {
            log.error("updateFieldById failed", e);
            return false;
        }
    }

    // ==================== 加密 ====================

    @Override
    public List<InMailSecret> secretTool(InMailSecret secret, Long memberId, boolean isUpdatePwd) {
        Map<String, Object> p = new HashMap<>();
        p.put("memberId", memberId);
        List<InMailSecret> list = DBAgent.find("FROM InMailSecret WHERE memberId=:memberId", p);
        if (secret != null) {
            if (isUpdatePwd && !list.isEmpty()) {
                InMailSecret s = list.get(0);
                s.setPwd(secret.getPwd());
                s.setUpdateDate(new java.sql.Timestamp(System.currentTimeMillis()));
                DBAgent.update(s);
            } else if (list.isEmpty()) {
                secret.setIdIfNew();
                secret.setCreateDate(new java.sql.Timestamp(System.currentTimeMillis()));
                DBAgent.save(secret);
            }
        }
        return list;
    }

    // ==================== 自动保存 ====================

    @Override
    public void cancelOrdeleteAutosaveState(boolean isDel, Timestamp firstAutosaveTime) {
        Map<String, Object> p = new HashMap<>();
        p.put("time", firstAutosaveTime);
        p.put("uid", AppContext.currentUserId());
        DBAgent.bulkUpdate("UPDATE InMailAffair SET state=3 WHERE firstAutosaveTime=:time AND senderId=:uid", p);
    }

    // ==================== 搜索/其他 ====================

    @Override
    public List<Map<String, Object>> getSearchDataStr(String key, String isS) {
        return dao.getSearchDataStr(key, isS);
    }

    @Override
    public Map<String, Object> getFormmainBySummaryId(Long summaryId, int code) {
        return Collections.emptyMap();
    }

    @Override
    public Object getOrgAccountByCode(String code, int type) {
        // 查询 org_department (type=1) 或 org_account (type=0)
        Map<String, Object> p = new HashMap<>();
        p.put("code", code);
        if (type == 1) {
            List<?> r = DBAgent.find("FROM OrgUnit WHERE code=:code", p);
            return r.isEmpty() ? null : r.get(0);
        } else {
            return null; // account 表暂无 code 字段映射
        }
    }

    @Override
    public List<Map<String, Object>> getCustomFloderEmailNumber() {
        return Collections.emptyList();
    }

    // ==================== 内部工具 (从 OA assemblyData 原样复制) ====================

    /**
     * 组装 BO 列表 —— 从 OA InternalMailManagerImpl.assemblyData 原样复制逻辑。
     */
    private List<InMailAffairBO> assemblyData(List<InMailAffair> list) {
        List<InMailAffairBO> boList = new ArrayList<>();
        if (list == null || list.isEmpty()) return boList;

        Map<Long, OrgMember> memberCache = new HashMap<>();
        Map<Long, OrgUnit> deptCache = new HashMap<>();

        for (InMailAffair a : list) {
            InMailAffairBO bo = new InMailAffairBO(a);
            bo.setId(a.getId());
            bo.setSubject(a.getSubject());
            bo.setReadFlag(a.getReadFlag());
            bo.setBrowseTime(a.getBrowseTime() != null ?
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(a.getBrowseTime()) : "");
            bo.setAttachmentsFlag(a.getAttachmentsFlag());
            bo.setSize(a.getSize() != null ? formatSize(a.getSize()) : "0KB");
            bo.setPassTheAudit(a.getPassTheAudit());
            bo.setCollect(a.getCollect());
            bo.setIsHandled(a.getIsHandled());
            bo.setSenderId(a.getSenderId());

            // 发件人
            OrgMember sender = getCachedMember(memberCache, a.getSenderId());
            bo.setSenderName(sender != null ? sender.getName() : "");
            if (sender != null) {
                OrgUnit dept = getCachedDept(deptCache, sender.getDepartmentId());
                bo.setRecUserDept(dept != null ? dept.getName() : "");
            }

            // 收件人
            OrgMember rec = getCachedMember(memberCache, a.getMemberId());
            bo.setRecUserName(rec != null ? rec.getName() : "");

            bo.setCreateDate(a.getCreateDate() != null ?
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(a.getCreateDate()) : "");

            boList.add(bo);
        }
        return boList;
    }

    private OrgMember getCachedMember(Map<Long, OrgMember> cache, Long id) {
        if (id == null) return null;
        return cache.computeIfAbsent(id, k -> DBAgent.get(OrgMember.class, k));
    }

    private OrgUnit getCachedDept(Map<Long, OrgUnit> cache, Long id) {
        if (id == null) return null;
        return cache.computeIfAbsent(id, k -> DBAgent.get(OrgUnit.class, k));
    }

    private String formatSize(Long size) {
        if (size == null || size == 0) return "0KB";
        if (size < 1024) return size + "B";
        if (size < 1048576) return (size / 1024) + "KB";
        return String.format("%.1fMB", size / 1048576.0);
    }
}
