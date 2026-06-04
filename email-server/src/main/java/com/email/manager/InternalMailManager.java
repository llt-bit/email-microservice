package com.email.manager;

import com.email.entity.*;
import com.email.platform.FlipInfo;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * 邮件核心业务接口 —— 从 OA InternalMailManager 精简。
 * 只保留 REST Controller 实际调用的方法签名。
 */
public interface InternalMailManager {

    // ===== 查询 =====
    InMailSummary getInMailSummaryById(long id);
    InMailAffair getInMailAffairById(long id);
    List<InMailAffair> getInMailAffairsByIds(List<Long> ids);
    List<InMailAffair> findAffairByInMailId(Long inMailId);
    List<InMailAffairBO> findAffairBOByInMailId(Long inMailId);

    // ===== 列表查询 =====
    FlipInfo findInboxAffair(FlipInfo fi, Long userId, Map<String, Object> param);
    FlipInfo findDraftAffair(FlipInfo fi, Long userId, Map<String, Object> param);
    FlipInfo findSentAffair(FlipInfo fi, Long userId, Map<String, Object> param);
    FlipInfo findDeleteAffair(FlipInfo fi, Long userId, Map<String, Object> param);
    FlipInfo findCollectAffair(FlipInfo fi, Long userId, Map<String, Object> param);
    FlipInfo findInboxAffairBySecret(FlipInfo fi, Map<String, Object> param);
    int findInboxNextAffair(Map<String, Object> param);

    // ===== 保存/操作 =====
    void saveInMailSummary(InMailSummary summary, List<InMailAffair> list, boolean isExist);
    void updateInMailAffair(InMailAffair affair);
    void updateInMailAffairs(List<InMailAffair> list);
    boolean updateAllAffairIsHandle(List<Long> affairIds, String flagType);
    void deleteAffair(String pageType, String affairId);
    void recoveryAffair(String pageType, String affairId);
    boolean updateFieldById(String field, Object fieldVal, List<Long> ids, Long id);

    // ===== 加密 =====
    List<InMailSecret> secretTool(InMailSecret secret, Long memberId, boolean isUpdatePwd);

    // ===== 自动保存 =====
    void cancelOrdeleteAutosaveState(boolean isDel, Timestamp firstAutosaveTime);

    // ===== 搜索/其他 =====
    List<Map<String, Object>> getSearchDataStr(String key, String isS);
    InMailSummary getInMailSummaryNewsAutosave(Timestamp firstAutosaveTime);
    Map<String, Object> getFormmainBySummaryId(Long summaryId, int code);
    Object getOrgAccountByCode(String code, int type);
    void executeMailClassify(Long summaryId, Long folderId);
    List<Map<String, Object>> getCustomFloderEmailNumber();
}
