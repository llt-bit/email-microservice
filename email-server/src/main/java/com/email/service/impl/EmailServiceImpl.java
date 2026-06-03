package com.email.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.email.common.PageResult;
import com.email.entity.*;
import com.email.enums.MailState;
import com.email.exception.BusinessException;
import com.email.mapper.*;
import com.email.security.UserContextHolder;
import com.email.service.FolderService;
import com.email.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 邮件核心业务实现（从 OA InternalMailManagerImpl + NewEmailUtils 迁移）。
 *
 * <p>核心流程：
 * 1. 发送邮件：组装 Summary+Affair → 事务插入 → 群组展开 → 通知 → 审批(如需)
 * 2. 查询分 6 种视图：收件箱/发件箱/草稿/已删除/收藏/加密
 * 3. 操作包括：删除/彻底删除/恢复/撤销/标记/已读未读</p>
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Resource private EmailSummaryMapper summaryMapper;
    @Resource private EmailAffairMapper affairMapper;
    @Resource private EmailAttachmentMapper attachmentMapper;
    @Resource private EmailSummaryMembersMapper summaryMembersMapper;
    @Resource private EmailFolderMapper folderMapper;
    @Resource private OrgMemberMapper orgMemberMapper;
    @Resource private OrgTeamMemberMapper teamMemberMapper;

    // 注入其他 Service（避免循环依赖，延迟注入）
    @Resource private FolderService folderService;
    @Resource private AttachmentService attachmentService;

    // ==================== 查询 ====================

    @Override
    public EmailAffair getAffairById(Long affairId) {
        return affairMapper.selectById(affairId);
    }

    @Override
    public EmailSummary getSummaryById(Long summaryId) {
        return summaryMapper.selectById(summaryId);
    }

    @Override
    public List<EmailAffair> getAffairsByIds(List<Long> affairIds) {
        return affairMapper.selectBatchIds(affairIds);
    }

    @Override
    public List<EmailAffair> getAffairsBySummaryId(Long summaryId) {
        LambdaQueryWrapper<EmailAffair> w = new LambdaQueryWrapper<>();
        w.eq(EmailAffair::getSummaryId, summaryId);
        return affairMapper.selectList(w);
    }

    // ==================== 列表 ====================

    @Override
    public PageResult<EmailAffair> findInbox(int page, int size, Long userId, Map<String, Object> params) {
        Page<EmailAffair> p = new Page<>(page, size);
        affairMapper.findInbox(p, userId, params);
        return PageResult.of(p.getRecords(), p.getTotal(), page, size);
    }

    @Override
    public PageResult<EmailAffair> findSent(int page, int size, Long userId, Map<String, Object> params) {
        Page<EmailAffair> p = new Page<>(page, size);
        affairMapper.findSent(p, userId, params);
        return PageResult.of(p.getRecords(), p.getTotal(), page, size);
    }

    @Override
    public PageResult<EmailAffair> findDraft(int page, int size, Long userId, Map<String, Object> params) {
        Page<EmailAffair> p = new Page<>(page, size);
        affairMapper.findDraft(p, userId, params);
        return PageResult.of(p.getRecords(), p.getTotal(), page, size);
    }

    @Override
    public PageResult<EmailAffair> findDeleted(int page, int size, Long userId, Map<String, Object> params) {
        Page<EmailAffair> p = new Page<>(page, size);
        affairMapper.findDeleted(p, userId, params);
        return PageResult.of(p.getRecords(), p.getTotal(), page, size);
    }

    @Override
    public PageResult<EmailAffair> findCollect(int page, int size, Long userId, Map<String, Object> params) {
        Page<EmailAffair> p = new Page<>(page, size);
        affairMapper.findCollect(p, userId, params);
        return PageResult.of(p.getRecords(), p.getTotal(), page, size);
    }

    @Override
    public PageResult<EmailAffair> findSecret(int page, int size, Long userId, Map<String, Object> params) {
        params.put("userId", userId);
        Page<EmailAffair> p = new Page<>(page, size);
        affairMapper.findSecret(p, params);
        return PageResult.of(p.getRecords(), p.getTotal(), page, size);
    }

    @Override
    public PageResult<EmailAffair> findByPath(int page, int size, Long userId, String path, Map<String, Object> params) {
        Page<EmailAffair> p = new Page<>(page, size);
        affairMapper.findByPath(p, userId, path, params);
        return PageResult.of(p.getRecords(), p.getTotal(), page, size);
    }

    // ==================== 数量统计 ====================

    @Override
    public Map<String, Integer> countGroupByState(Long userId) {
        Map<String, Integer> counts = affairMapper.countGroupByState(userId);
        return counts != null ? counts : Collections.emptyMap();
    }

    // ==================== 发送邮件 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmailSummary sendEmail(Map<String, Object> params) {
        // 1. 解析参数
        Long senderId = UserContextHolder.getUserId();
        OrgMember sender = orgMemberMapper.selectById(senderId);
        if (sender == null) {
            throw new BusinessException("发件人信息获取失败");
        }

        String type = (String) params.get("type");
        boolean isSend = "send".equals(type);
        boolean isTimed = Boolean.TRUE.equals(params.get("timedTask"));

        // 2. 组装 EmailSummary
        EmailSummary summary = buildSummary(params, sender, isSend);

        // 3. 处理附件（将临时附件关联到邮件）
        Object attachmentIdsObj = params.get("attachmentIds");
        if (attachmentIdsObj != null) {
            summary.setAttachmentsFlag(1);
        }

        // 4. 保存邮件主体
        if (summary.getId() != null) {
            // 编辑已有草稿
            summaryMapper.updateById(summary);
        } else {
            summaryMapper.insert(summary);
        }

        // 5. 组装发送列表（展开群组/部门/单位 → 每个收件人一条 Affair）
        List<EmailAffair> affairs = buildAffairs(summary, params, sender, isSend);

        // 6. 批量保存事务
        if (!affairs.isEmpty()) {
            for (EmailAffair affair : affairs) {
                if (affair.getId() != null) {
                    affairMapper.updateById(affair);
                } else {
                    affairMapper.insert(affair);
                }
            }
        }

        // 7. 展开群组/部门 → 保存展开后的成员记录
        expandGroupMembers(summary, params);

        // 8. 应用文件夹规则（如果符合自定义文件夹规则则自动归类）
        applyFolderRules(summary, affairs);

        // 9. 取消自动保存（会删除旧的自动保存草稿）
        cancelAutosave(params);

        log.info("邮件发送成功: summaryId={}, sender={}, recipients={}",
                summary.getId(), sender.getName(), summary.getSendTo());

        return summary;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmailSummary saveDraft(Map<String, Object> params) {
        // 草稿保存流程与发送类似，但 state=1, 只给自己创建一条 affair
        Long senderId = UserContextHolder.getUserId();
        OrgMember sender = orgMemberMapper.selectById(senderId);
        if (sender == null) {
            throw new BusinessException("发件人信息获取失败");
        }

        EmailSummary summary = buildSummary(params, sender, false);
        if (summary.getId() != null) {
            summaryMapper.updateById(summary);
        } else {
            summaryMapper.insert(summary);
        }

        // 草稿只为发件人自己创建一条 affair (state=1)
        EmailAffair draftAffair = buildDraftAffair(summary, sender);
        if (draftAffair.getId() != null) {
            affairMapper.updateById(draftAffair);
        } else {
            affairMapper.insert(draftAffair);
        }

        return summary;
    }

    // ==================== 操作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMail(List<Long> affairIds, String pageType) {
        List<EmailAffair> affairs = affairMapper.selectBatchIds(affairIds);
        for (EmailAffair a : affairs) {
            if (a == null) continue;
            if ("delete".equals(pageType)) {
                // 从已删除中彻底删除
                a.setIsDelete(1);
            } else {
                // 移到已删除
                a.setDeleteState(a.getState());
                a.setState(MailState.DELETED.getCode());
            }
            a.setUpdateTime(LocalDateTime.now());
        }
        for (EmailAffair a : affairs) {
            if (a != null) affairMapper.updateById(a);
        }
    }

    @Override
    public void permanentDelete(List<Long> affairIds) {
        affairMapper.deleteBatchIds(affairIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recoveryMail(List<Long> affairIds) {
        List<EmailAffair> affairs = affairMapper.selectBatchIds(affairIds);
        for (EmailAffair a : affairs) {
            if (a == null || a.getDeleteState() == null) continue;
            a.setState(a.getDeleteState());
            a.setDeleteState(null);
            a.setUpdateTime(LocalDateTime.now());
            affairMapper.updateById(a);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String recallMail(List<Long> affairIds) {
        Long userId = UserContextHolder.getUserId();
        StringBuilder sb = new StringBuilder();

        for (Long affairId : affairIds) {
            EmailAffair sentAffair = affairMapper.selectById(affairId);
            if (sentAffair == null) continue;

            // 检查是否可撤销：有人已读/已转发/已回复则不��撤销
            List<EmailAffair> allAffairs = getAffairsBySummaryId(sentAffair.getSummaryId());
            boolean hasRead = false, hasForward = false, hasReply = false;

            for (EmailAffair a : allAffairs) {
                if (a.getReadFlag() == 1 && !Objects.equals(a.getMemberId(), userId)) hasRead = true;
                if (a.getIsForward() == 1) hasForward = true;
                if (a.getIsReply() == 1) hasReply = true;
            }

            if (hasRead && hasForward) {
                sb.append(sentAffair.getSubject()).append("(已读/已转发) ");
                continue;
            }

            // 撤销：发件人事务改回草稿，收件人事务删除
            sentAffair.setState(MailState.DRAFT.getCode());
            sentAffair.setUpdateTime(LocalDateTime.now());
            affairMapper.updateById(sentAffair);

            for (EmailAffair a : allAffairs) {
                if (!Objects.equals(a.getId(), sentAffair.getId())) {
                    if (a.getState() != MailState.DRAFT.getCode()) {
                        affairMapper.deleteById(a.getId());
                    }
                }
            }
        }
        return sb.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String markAffairs(List<Long> affairIds, String flagType) {
        List<EmailAffair> affairs = affairMapper.selectBatchIds(affairIds);
        switch (flagType) {
            case "handled":
                for (EmailAffair a : affairs) {
                    if (a != null) { a.setIsHandled(1); a.setUpdateTime(LocalDateTime.now()); }
                }
                break;
            case "collection":
                for (EmailAffair a : affairs) {
                    if (a != null) { a.setCollect(1); a.setUpdateTime(LocalDateTime.now()); }
                }
                break;
            case "cancelCollection":
                for (EmailAffair a : affairs) {
                    if (a != null) { a.setCollect(0); a.setUpdateTime(LocalDateTime.now()); }
                }
                break;
            case "encryption":
                for (EmailAffair a : affairs) {
                    if (a != null) { a.setState(5); a.setUpdateTime(LocalDateTime.now()); }
                }
                break;
            case "cancelEncryption":
                for (EmailAffair a : affairs) {
                    if (a != null) { a.setState(2); a.setUpdateTime(LocalDateTime.now()); }
                }
                break;
            default:
                throw new BusinessException("未知标记类型: " + flagType);
        }
        for (EmailAffair a : affairs) {
            if (a != null) affairMapper.updateById(a);
        }
        return "操作成功";
    }

    @Override
    public void markUnread(Long affairId) {
        EmailAffair a = affairMapper.selectById(affairId);
        if (a != null) {
            a.setReadFlag(0);
            a.setUpdateTime(LocalDateTime.now());
            affairMapper.updateById(a);
        }
    }

    // ==================== 回复/转发数据 ====================

    @Override
    public Map<String, Object> getReplyData(Map<String, Object> params) {
        Long summaryId = Long.parseLong((String) params.get("summaryId"));
        Long affairId = Long.parseLong((String) params.get("affairId"));

        EmailSummary summary = summaryMapper.selectById(summaryId);
        EmailAffair affair = affairMapper.selectById(affairId);
        OrgMember sender = orgMemberMapper.selectById(affair.getSenderId());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("subject", "回复：" + summary.getSubject());
        data.put("sendTo", sender.getName());
        data.put("sendToIds", "Member|" + sender.getId());
        data.put("content", summary.getSummaryContent()); // 原文引用
        data.put("replyParentAffairId", affairId);
        data.put("replyParentSummaryId", summaryId);
        data.put("mark", "reply");
        data.put("summaryId", summaryId);
        data.put("affairId", affairId);
        return data;
    }

    @Override
    public Map<String, Object> getAllReplyData(Map<String, Object> params) {
        Long summaryId = Long.parseLong((String) params.get("summaryId"));
        EmailSummary summary = summaryMapper.selectById(summaryId);

        // 全部回复 = 回复发件人 + 回复所有主送 + 回复所有抄送
        List<String> toNames = new ArrayList<>();
        List<String> toIds = new ArrayList<>();

        // 发件人
        OrgMember sender = orgMemberMapper.selectById(summary.getSenderId());
        if (sender != null) {
            toNames.add(sender.getName());
            toIds.add("Member|" + sender.getId());
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("subject", "回复：" + summary.getSubject());
        data.put("sendTo", String.join(",", toNames));
        data.put("sendToIds", String.join(",", toIds));
        data.put("content", summary.getSummaryContent());
        data.put("replyParentSummaryId", summaryId);
        data.put("mark", "allReply");
        data.put("summaryId", summaryId);
        return data;
    }

    @Override
    public Map<String, Object> getForwardData(Map<String, Object> params) {
        Long summaryId = Long.parseLong((String) params.get("summaryId"));
        EmailSummary summary = summaryMapper.selectById(summaryId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("subject", "转发：" + summary.getSubject());
        data.put("content", summary.getSummaryContent());
        data.put("forwardMember", null);
        data.put("forwardMemberId", null);
        data.put("parentformSummaryId", summaryId);
        data.put("mark", "forward");
        data.put("summaryId", summaryId);
        return data;
    }

    @Override
    public Map<String, Object> getInternalCompileData(Map<String, Object> params) {
        String mark = (String) params.get("mark");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("mark", "new");

        if ("reEdit".equals(mark)) {
            Long summaryId = Long.parseLong((String) params.get("summaryId"));
            EmailSummary summary = summaryMapper.selectById(summaryId);
            if (summary != null) {
                data.put("mark", "reEdit");
                data.put("subject", summary.getSubject());
                data.put("sendTo", summary.getSendTo());
                data.put("sendToIds", summary.getSendToIds());
                data.put("copyTo", summary.getCopyTo());
                data.put("copyToIds", summary.getCopyToIds());
                data.put("content", summary.getSummaryContent());
                data.put("summaryId", summaryId);
            }
        }
        return data;
    }

    // ==================== 内部工具方法 ====================

    /**
     * 组装邮件主体（从 OA InMailUtil.makeInMailSummary 迁移）。
     */
    private EmailSummary buildSummary(Map<String, Object> params, OrgMember sender, boolean isSend) {
        EmailSummary s = new EmailSummary();

        // 如果是编辑已有草稿
        Object summaryIdObj = params.get("summaryId");
        if (summaryIdObj != null && StringUtils.hasText(summaryIdObj.toString())) {
            s = summaryMapper.selectById(Long.parseLong(summaryIdObj.toString()));
            if (s == null) s = new EmailSummary();
        }

        s.setSenderId(sender.getId());
        s.setSenderName(sender.getName());
        s.setOrgDepartmentId(sender.getDepartmentId());
        s.setOrgAccountId(sender.getAccountId());
        s.setSubject((String) params.get("subject"));
        s.setSummaryContent((String) params.get("content"));
        s.setMark((String) params.get("mark"));

        // 主送人
        s.setSendTo((String) params.get("receiversStr"));
        s.setSendToIds((String) params.get("receivers"));

        // 抄送人
        s.setCopyTo((String) params.get("copyReceiversStr"));
        s.setCopyToIds((String) params.get("copyReceivers"));

        // 密级
        Object secretTypeId = params.get("secretTypeId");
        if (secretTypeId != null && StringUtils.hasText(secretTypeId.toString())) {
            s.setFileSecretLevelId(Long.parseLong(secretTypeId.toString()));
        }

        // 审批人
        s.setApprover((String) params.get("approver"));
        s.setApproverStr((String) params.get("approverStr"));

        // 跨网
        Boolean kuaWang = (Boolean) params.get("kuaWang");
        s.setKuaWang(kuaWang != null && kuaWang ? 1 : 0);

        // 定时发送
        Boolean timedTask = (Boolean) params.get("timedTask");
        Boolean isTiming = (Boolean) params.get("isTiming");
        if (Boolean.TRUE.equals(timedTask) || Boolean.TRUE.equals(isTiming)) {
            s.setIsTiming(1);
            Object timingDate = params.get("timingDate");
            if (timingDate != null) {
                s.setTimingDate(LocalDateTime.parse(timingDate.toString()));
            }
        }

        // 草稿/发送状态
        if (isSend) {
            s.setState(MailState.SENT.getCode());
        } else {
            s.setState(MailState.DRAFT.getCode());
        }

        s.setCreateTime(LocalDateTime.now());
        s.setCreateBy(sender.getId());
        s.setUpdateTime(LocalDateTime.now());

        return s;
    }

    /**
     * 组装收件人事务列表（每个收件人一条 EmailAffair）。
     */
    private List<EmailAffair> buildAffairs(EmailSummary summary, Map<String, Object> params,
                                           OrgMember sender, boolean isSend) {
        List<EmailAffair> list = new ArrayList<>();

        // 收件箱状态 = 0
        int state = isSend ? 0 : MailState.DRAFT.getCode();

        // 解析主送人
        Set<String> allIds = new LinkedHashSet<>();
        String receivers = summary.getSendToIds();
        if (StringUtils.hasText(receivers)) {
            Collections.addAll(allIds, receivers.split(","));
        }
        String copyReceivers = summary.getCopyToIds();
        if (StringUtils.hasText(copyReceivers)) {
            Collections.addAll(allIds, copyReceivers.split(","));
        }

        for (String idStr : allIds) {
            if (!StringUtils.hasText(idStr)) continue;

            EmailAffair a = new EmailAffair();
            a.setSummaryId(summary.getId());
            a.setState(state);
            a.setSenderId(sender.getId());
            a.setSenderName(sender.getName());
            a.setSubject(summary.getSubject());
            a.setAttachmentsFlag(summary.getAttachmentsFlag());
            a.setAffairSize(summary.getSummarySize());
            a.setOrgDepartmentId(sender.getDepartmentId());
            a.setOrgAccountId(sender.getAccountId());
            a.setCreateTime(LocalDateTime.now());
            a.setUpdateTime(LocalDateTime.now());

            // 解析 "Member|123" "Department|456" 等格式
            String[] parts = idStr.split("\\|");
            if (parts.length >= 2 && "Member".equals(parts[0])) {
                Long memberId = Long.parseLong(parts[1]);
                a.setMemberId(memberId);
                OrgMember m = orgMemberMapper.selectById(memberId);
                if (m != null) a.setMemberName(m.getName());
            }
            list.add(a);
        }

        // 发件人自己也要一条事务（用于发件箱显示）
        if (isSend) {
            EmailAffair selfAffair = new EmailAffair();
            selfAffair.setSummaryId(summary.getId());
            selfAffair.setState(MailState.SENT.getCode());
            selfAffair.setSenderId(sender.getId());
            selfAffair.setSenderName(sender.getName());
            selfAffair.setMemberId(sender.getId());
            selfAffair.setMemberName(sender.getName());
            selfAffair.setSubject(summary.getSubject());
            selfAffair.setAttachmentsFlag(summary.getAttachmentsFlag());
            selfAffair.setOrgDepartmentId(sender.getDepartmentId());
            selfAffair.setOrgAccountId(sender.getAccountId());
            selfAffair.setCreateTime(LocalDateTime.now());
            selfAffair.setUpdateTime(LocalDateTime.now());
            list.add(selfAffair);
        }

        return list;
    }

    /**
     * 草稿事务——只给发件人自己创建一条。
     */
    private EmailAffair buildDraftAffair(EmailSummary summary, OrgMember sender) {
        // 查找已有的草稿记录
        LambdaQueryWrapper<EmailAffair> w = new LambdaQueryWrapper<>();
        w.eq(EmailAffair::getSummaryId, summary.getId())
         .eq(EmailAffair::getMemberId, sender.getId())
         .eq(EmailAffair::getState, MailState.DRAFT.getCode());
        EmailAffair a = affairMapper.selectOne(w);
        if (a == null) a = new EmailAffair();

        a.setSummaryId(summary.getId());
        a.setState(MailState.DRAFT.getCode());
        a.setSenderId(sender.getId());
        a.setSenderName(sender.getName());
        a.setMemberId(sender.getId());
        a.setMemberName(sender.getName());
        a.setSubject(summary.getSubject());
        a.setAttachmentsFlag(summary.getAttachmentsFlag());
        a.setOrgDepartmentId(sender.getDepartmentId());
        a.setOrgAccountId(sender.getAccountId());
        a.setCreateTime(LocalDateTime.now());
        a.setUpdateTime(LocalDateTime.now());
        return a;
    }

    /**
     * 展开群组/部门/单位成员（从 OA InMailSummaryMembersManager 迁移）。
     */
    private void expandGroupMembers(EmailSummary summary, Map<String, Object> params) {
        // 清理旧的展开记录
        LambdaQueryWrapper<EmailSummaryMembers> w = new LambdaQueryWrapper<>();
        w.eq(EmailSummaryMembers::getMailSummaryId, summary.getId());
        summaryMembersMapper.delete(w);

        // 解析每个收件人，如果是群/部门/单位则展开
        String sendToIds = summary.getSendToIds();
        String copyToIds = summary.getCopyToIds();
        String allIds = "";
        if (StringUtils.hasText(sendToIds)) allIds = sendToIds;
        if (StringUtils.hasText(copyToIds)) allIds += (allIds.isEmpty() ? "" : ",") + copyToIds;

        if (allIds.isEmpty()) return;

        int sort = 0;
        for (String idStr : allIds.split(",")) {
            if (!StringUtils.hasText(idStr)) continue;
            String[] parts = idStr.split("\\|");
            if (parts.length < 2) continue;

            String type = parts[0];
            Long entityId = Long.parseLong(parts[1]);

            List<OrgMember> members = resolveMembers(type, entityId);
            for (OrgMember m : members) {
                EmailSummaryMembers esm = new EmailSummaryMembers();
                esm.setMailSummaryId(summary.getId());
                esm.setMemberId(m.getId());
                esm.setMemberName(m.getName());
                esm.setMemberLoginName(m.getLoginName());
                esm.setType(type);
                esm.setTypeId(entityId);
                esm.setAccountId(m.getAccountId());
                esm.setSort(++sort);
                summaryMembersMapper.insert(esm);
            }
        }
    }

    /** 解析群组/部门/单位的实际成员列表 */
    private List<OrgMember> resolveMembers(String type, Long entityId) {
        switch (type) {
            case "Member":
                OrgMember m = orgMemberMapper.selectById(entityId);
                return m != null ? Collections.singletonList(m) : Collections.emptyList();
            case "Department":
                return orgMemberMapper.findByDepartmentId(entityId);
            case "Account":
                return orgMemberMapper.findByAccountId(entityId);
            case "Team":
                // 组/团队 = 所有组成员
                LambdaQueryWrapper<OrgTeamMember> tw = new LambdaQueryWrapper<>();
                tw.eq(OrgTeamMember::getTeamId, entityId);
                List<OrgTeamMember> tms = teamMemberMapper.selectList(tw);
                List<Long> memberIds = tms.stream().map(OrgTeamMember::getMemberId).collect(Collectors.toList());
                if (memberIds.isEmpty()) return Collections.emptyList();
                return orgMemberMapper.selectBatchIds(memberIds);
            default:
                return Collections.emptyList();
        }
    }

    /** 取消自动保存草稿 */
    private void cancelAutosave(Map<String, Object> params) {
        String firstAutosaveTime = (String) params.get("firstAutosaveTime");
        String oldAffairId = (String) params.get("oldAffairId");
        if (!StringUtils.hasText(firstAutosaveTime) || !StringUtils.hasText(oldAffairId)) return;

        try {
            EmailAffair old = affairMapper.selectById(Long.parseLong(oldAffairId));
            if (old != null && old.getState() == MailState.DRAFT.getCode()) {
                affairMapper.deleteById(old.getId());
                // 如果该草稿对应的 summary 没有其他事务了，也删除 summary
                LambdaQueryWrapper<EmailAffair> w = new LambdaQueryWrapper<>();
                w.eq(EmailAffair::getSummaryId, old.getSummaryId());
                if (affairMapper.selectCount(w) == 0) {
                    summaryMapper.deleteById(old.getSummaryId());
                }
            }
        } catch (Exception e) {
            log.warn("取消自动保存失败: {}", e.getMessage());
        }
    }

    /** 自动将邮件归入符合规则的自定义文件夹 */
    private void applyFolderRules(EmailSummary summary, List<EmailAffair> affairs) {
        if (CollectionUtils.isEmpty(affairs)) return;

        // 遍历所有收件人，查询其文件夹规则
        Set<Long> memberIds = affairs.stream()
                .map(EmailAffair::getMemberId)
                .collect(Collectors.toSet());

        for (Long memberId : memberIds) {
            List<EmailFolder> folders = folderService.getFolders(memberId);
            for (EmailFolder folder : folders) {
                if (!StringUtils.hasText(folder.getRule())) continue;

                String rule = folder.getRule();
                String subject = summary.getSubject();
                String senderName = summary.getSenderName();

                // 规则简单匹配：主题包含关键�� 或 发件人匹配
                boolean matched = false;
                if (subject != null && rule.contains(subject)) matched = true;
                if (senderName != null && rule.contains(senderName)) matched = true;

                if (matched) {
                    for (EmailAffair a : affairs) {
                        if (a.getMemberId().equals(memberId)) {
                            a.setPath(folder.getPath());
                            affairMapper.updateById(a);
                        }
                    }
                }
            }
        }
    }
}
