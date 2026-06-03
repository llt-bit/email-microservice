package com.email.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.email.common.PageResult;
import com.email.entity.*;

import java.util.List;
import java.util.Map;

/**
 * 邮件核心业务接口（从 OA InternalMailManager 迁移）。
 */
public interface EmailService {

    // ==================== 查询 ====================

    /** 获取邮件事务 */
    EmailAffair getAffairById(Long affairId);

    /** 获取邮件主体 */
    EmailSummary getSummaryById(Long summaryId);

    /** 获取一批邮件事务 */
    List<EmailAffair> getAffairsByIds(List<Long> affairIds);

    /** 获取某封邮件的所有事务（用于回执查看） */
    List<EmailAffair> getAffairsBySummaryId(Long summaryId);

    // ==================== 列表查询 ====================

    /** 收件箱列表 */
    PageResult<EmailAffair> findInbox(int page, int size, Long userId, Map<String, Object> params);

    /** 发件箱列表 */
    PageResult<EmailAffair> findSent(int page, int size, Long userId, Map<String, Object> params);

    /** 草稿箱列表 */
    PageResult<EmailAffair> findDraft(int page, int size, Long userId, Map<String, Object> params);

    /** 已删除列表 */
    PageResult<EmailAffair> findDeleted(int page, int size, Long userId, Map<String, Object> params);

    /** 收藏列表 */
    PageResult<EmailAffair> findCollect(int page, int size, Long userId, Map<String, Object> params);

    /** 加密箱列表 */
    PageResult<EmailAffair> findSecret(int page, int size, Long userId, Map<String, Object> params);

    /** 自定义文件夹列表 */
    PageResult<EmailAffair> findByPath(int page, int size, Long userId, String path, Map<String, Object> params);

    // ==================== 数量统计 ====================

    /** 聚合获取各文件夹邮件数量（减少 N 次查询） */
    Map<String, Integer> countGroupByState(Long userId);

    // ==================== 发送/保存 ====================

    /**
     * 发送邮件。
     * <p>主要流程：校验→组装 EmailSummary+EmailAffair→保存→通知→审批(如需)</p>
     */
    EmailSummary sendEmail(Map<String, Object> params);

    /**
     * 保存草稿。
     */
    EmailSummary saveDraft(Map<String, Object> params);

    // ==================== 操作 ====================

    /** 删除邮件（移到已删除） */
    void deleteMail(List<Long> affairIds, String pageType);

    /** 彻底删除（从已删除中删除） */
    void permanentDelete(List<Long> affairIds);

    /** 恢复已删除的邮件 */
    void recoveryMail(List<Long> affairIds);

    /** 撤销已发送的邮件 */
    String recallMail(List<Long> affairIds);

    /** 标记操作（已办/收藏/加密） */
    String markAffairs(List<Long> affairIds, String flagType);

    /** 标记未读 */
    void markUnread(Long affairId);

    // ==================== 回复/转发数据 ====================

    /** 获取回复弹窗数据 */
    Map<String, Object> getReplyData(Map<String, Object> params);

    /** 获取全部回复弹窗数据 */
    Map<String, Object> getAllReplyData(Map<String, Object> params);

    /** 获取转发弹窗数据 */
    Map<String, Object> getForwardData(Map<String, Object> params);

    /** 获取编辑/草稿弹窗数据 */
    Map<String, Object> getInternalCompileData(Map<String, Object> params);
}
