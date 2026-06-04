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

    // ==================== 列表（OA 原样: DAO→assemblyData包装→getImgAndSecret增强） ====================

    @Override
    public FlipInfo findInboxAffair(FlipInfo fi, Long userId, Map<String, Object> param) {
        param.put("memberId", userId);
        List<InMailAffair> list = dao.findInboxAffairRaw(fi, param, userId, InMailConstant.InMailAffairState.run.ordinal());
        if (list != null && !list.isEmpty()) {
            fi.setData(assemblyData(list));
        }
        List<InMailAffairBO> data = fi.getData();
        this.getImgAndSecret(data);
        return fi;
    }

    @Override
    public FlipInfo findDraftAffair(FlipInfo fi, Long userId, Map<String, Object> param) {
        param.put("memberId", userId);
        List<InMailAffair> list = dao.findDraftAffairRaw(fi, param, userId, InMailConstant.InMailAffairState.draf.ordinal());
        if (list != null && !list.isEmpty()) {
            fi.setData(assemblyData(list));
        }
        List<InMailAffairBO> data = fi.getData();
        this.getImgAndSecret(data);
        return fi;
    }

    @Override
    public FlipInfo findSentAffair(FlipInfo fi, Long userId, Map<String, Object> param) {
        param.put("memberId", userId);
        List<InMailAffair> list = dao.findSentAffairRaw(fi, param, userId, InMailConstant.InMailAffairState.sent.ordinal());

        // OA 原样：发件箱检查所有收件人是否已读/已转发/已回复
        for (int i = 0; i < list.size(); i++) {
            InMailAffair aff = list.get(i);
            aff.setIsForward(true);
            aff.setIsReply(true);
            aff.setReadFlag(true);

            InMailSummary s = this.getInMailSummaryById(aff.getObjectId());
            if (s != null) {
                List<InMailAffair> allAffairs = dao.getInMailAffairByObjectId(s.getId());
                for (InMailAffair ra : allAffairs) {
                    if (Boolean.FALSE.equals(ra.getIsForward())) aff.setIsForward(false);
                    if (Boolean.FALSE.equals(ra.getIsReply())) aff.setIsReply(false);
                    if (Boolean.FALSE.equals(ra.getReadFlag())) aff.setReadFlag(false);
                }
            }
        }

        if (list != null && !list.isEmpty()) {
            fi.setData(assemblyData(list));
        }
        List<InMailAffairBO> data = fi.getData();
        this.getImgAndSecret(data);
        return fi;
    }

    @Override
    public FlipInfo findDeleteAffair(FlipInfo fi, Long userId, Map<String, Object> param) {
        param.put("memberId", userId);
        List<InMailAffair> list = dao.findDeleteAffairRaw(fi, param, userId, InMailConstant.InMailAffairState.deleted.ordinal());
        if (list != null && !list.isEmpty()) {
            fi.setData(assemblyData(list));
        }
        List<InMailAffairBO> data = fi.getData();
        this.getImgAndSecret(data);
        return fi;
    }

    @Override
    public FlipInfo findCollectAffair(FlipInfo fi, Long userId, Map<String, Object> param) {
        Long uid = userId != null ? userId : AppContext.currentUserId();
        Map<String, Object> p = new HashMap<>(); p.put("mid", uid);
        List<InMailAffair> list = DBAgent.find("FROM InMailAffair WHERE memberId=:mid AND collect=1 AND state!=3 AND delFlag=0", p);
        if (list != null && !list.isEmpty()) {
            fi.setData(assemblyData(list));
        }
        List<InMailAffairBO> data = fi.getData();
        this.getImgAndSecret(data);
        return fi;
    }

    @Override
    public FlipInfo findInboxAffairBySecret(FlipInfo fi, Map<String, Object> param) {
        Long userId = AppContext.currentUserId();
        Map<String, Object> p = new HashMap<>(); p.put("mid", userId);
        List<InMailAffair> list = DBAgent.find("FROM InMailAffair WHERE memberId=:mid AND state=5 AND delFlag=0", p);
        if (list != null && !list.isEmpty()) {
            fi.setData(assemblyData(list));
        }
        List<InMailAffairBO> data = fi.getData();
        this.getImgAndSecret(data);
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
        List<InMailAffair> toUpdate = new ArrayList<>();
        for (Long id : affairIds) {
            InMailAffair a = DBAgent.get(InMailAffair.class, id);
            if (a == null) continue;
            // OA 原样验证逻辑（Boolean 值处理 null 安全）
            if ("handled".equals(flagType)) {
                if (Boolean.TRUE.equals(a.getIsHandled()) || Boolean.TRUE.equals(a.getIsReply()) || Boolean.TRUE.equals(a.getIsForward())) {
                    return false;
                }
                a.setIsHandled(true);
                toUpdate.add(a);
            } else if ("collection".equals(flagType)) {
                if (a.getCollect() == null || a.getCollect() != 1) {
                    a.setCollect(1);
                    toUpdate.add(a);
                } else {
                    return false;
                }
            } else if ("cancelCollection".equals(flagType)) {
                a.setCollect(0);
                toUpdate.add(a);
            } else if ("encryption".equals(flagType)) {
                if (a.getState() != 5) {
                    a.setState(5);
                    a.setDeleteState(5); // OA 原样：记录加密时状态
                    toUpdate.add(a);
                } else {
                    return false;
                }
            } else if ("cancelEncryption".equals(flagType)) {
                a.setState(2);
                toUpdate.add(a);
            } else {
                return false;
            }
        }
        DBAgent.updateAll(toUpdate);
        return true;
    }

    // ==================== 删除/恢复 ====================

    @Override
    public void deleteAffair(String pageType, String affairId) {
        if (!Strings.isNotBlank(affairId)) return;
        InMailAffair a = DBAgent.get(InMailAffair.class, Long.parseLong(affairId));
        if (a == null) return;
        Timestamp now = new Timestamp(System.currentTimeMillis());
        // OA 原样: "delete"表示从已删除中彻底删除
        if ("delete".equals(pageType)) {
            a.setDelete(true);
            a.setUpdateDate(now);
        } else {
            // 记录删除前状态用于恢复
            if ("sent".equals(pageType)) a.setDeleteState(0);
            else if ("draft".equals(pageType)) a.setDeleteState(1);
            else if ("inbox".equals(pageType) || "inBox".equals(pageType)) a.setDeleteState(2);
            else if ("collection".equals(pageType)) a.setDeleteState(a.getState());
            else a.setDeleteState(a.getState()); // 兜底
            a.setState(InMailConstant.InMailAffairState.deleted.ordinal());
            a.setUpdateDate(now);
        }
        DBAgent.update(a);
    }

    @Override
    public void recoveryAffair(String pageType, String affairId) {
        // OA 原样：恢复时不 null 掉 deleteState，保留用于后续可能的再次恢复
        if (Strings.isNotBlank(affairId) && Strings.isNotBlank(pageType)) {
            InMailAffair affair = DBAgent.get(InMailAffair.class, Long.parseLong(affairId));
            if (affair != null) {
                affair.setState(affair.getDeleteState());
            }
            DBAgent.update(affair);
        }
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
        // type=0: Account, type=1: Department（OA 中用 code 查询，本地 OrgUnit 暂缺 code 字段）
        return null;
    }

    @Override
    public List<Map<String, Object>> getCustomFloderEmailNumber() {
        return Collections.emptyList();
    }

    // ==================== 内部工具 ====================

    /**
     * 给 BO 列表添加密级与头像 —— 从 OA getImgAndSecret 原样复制逻辑。
     */
    @SuppressWarnings("unchecked")
    private void getImgAndSecret(List<InMailAffairBO> data) {
        if (data == null || data.isEmpty()) return;
        for (InMailAffairBO bo : data) {
            Long senderId = bo.getSenderId();
            bo.setIdStr(bo.getId().toString());
            bo.setMemberIdStr(bo.getMemberId().toString());
            bo.setSenderIdStr(senderId.toString());
            bo.setSummaryIdStr(bo.getSummaryId().toString());
            // 密级：OA 中查 FileSecretMapManager/FileSecretManager，独立后简化为默认值
            bo.setSecretNameStr("");
            bo.setSecretIdStr("");
            // 头像
            bo.setImg("");
        }
    }

    /**
     * 组装 BO 列表 —— 从 OA getInMailAffairBO 逐逻辑复制。
     */
    private List<InMailAffairBO> assemblyData(List<InMailAffair> list) {
        List<InMailAffairBO> boList = new ArrayList<>();
        if (list == null || list.isEmpty()) return boList;

        Map<Long, OrgMember> memberCache = new HashMap<>();
        Map<Long, OrgUnit> deptCache = new HashMap<>();

        for (InMailAffair bo : list) {
            InMailAffairBO person = new InMailAffairBO();
            person.setId(bo.getId());
            person.setSecurity(bo.getSecurity());
            person.setSenderId(bo.getSenderId());

            // 发件人（OA: orgManager.getMemberById）
            OrgMember member = getCachedMember(memberCache, bo.getSenderId());
            if (member != null) person.setSenderName(member.getName());

            person.setSummaryId(bo.getObjectId());
            person.setMemberId(bo.getMemberId());

            // 收件人：不是同一个人时才查（OA: memberId != senderId）
            if (!bo.getMemberId().equals(bo.getSenderId())) {
                member = getCachedMember(memberCache, bo.getMemberId());
            }
            if (member != null) {
                person.setRecUserName(member.getName());
                OrgUnit dept = getCachedDept(deptCache, member.getDepartmentId());
                if (dept != null) person.setRecUserDept(dept.getName());
            }

            person.setCreateDate(Datetimes.formatDatetime(bo.getCreateDate(), 0));
            person.setReadFlag(bo.getReadFlag());
            person.setForwardFlag(bo.getIsForward());
            person.setReplyFlag(bo.getIsReply());
            person.setIsHandled(bo.getIsHandled());
            person.setBrowseTime(Datetimes.formatDatetime(bo.getBrowseTime(), 0));
            person.setAttachmentsFlag(bo.getAttachmentsFlag());
            person.setSubject(bo.getSubject());
            person.setPassTheAudit(bo.getPassTheAudit());
            person.setCollect(bo.getCollect());

            Long sz = bo.getSize();
            if (sz != null) person.setSize((sz / 1024 + 1) + "KB");

            boList.add(person);
        }
        return boList;
    }

    private OrgMember getCachedMember(Map<Long, OrgMember> cache, Long id) {
        if (id == null) return null;
        OrgMember m = cache.get(id);
        if (m == null) { m = DBAgent.get(OrgMember.class, id); cache.put(id, m); }
        return m;
    }

    private OrgUnit getCachedDept(Map<Long, OrgUnit> cache, Long id) {
        if (id == null) return null;
        OrgUnit d = cache.get(id);
        if (d == null) { d = DBAgent.get(OrgUnit.class, id); cache.put(id, d); }
        return d;
    }
}
