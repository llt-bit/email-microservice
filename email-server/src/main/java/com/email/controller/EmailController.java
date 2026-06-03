package com.email.controller;

import com.email.common.PageResult;
import com.email.common.R;
import com.email.entity.EmailAffair;
import com.email.entity.EmailSummary;
import com.email.security.UserContextHolder;
import com.email.service.AttachmentService;
import com.email.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * 邮件 REST API（从 OA EmailPCResource + EmailPCResourceFunction 迁移）。
 *
 * <p>原路径 /seeyon/rest/newemail → 改为 /api/email。</p>
 */
@Slf4j
@RestController
@RequestMapping("/email")
public class EmailController {

    @Resource private EmailService emailService;
    @Resource private AttachmentService attachmentService;

    // ==================== 列表 ====================

    @PostMapping("/list")
    public R<PageResult<EmailAffair>> list(@RequestBody Map<String, Object> params) {
        Long userId = UserContextHolder.getUserId();
        String type = (String) params.get("type");
        int pageNo = params.get("pageNo") != null ? (Integer) params.get("pageNo") : 1;
        int pageSize = params.get("pageSize") != null ? (Integer) params.get("pageSize") : 20;

        // 解析搜索条件
        Map<String, Object> searchParams = parseSearchParams(params);

        // 收件箱子类型（全部/未读/已办/未办）
        String inBoxType = (String) params.get("inBoxType");
        if (inBoxType != null) searchParams.put("readType", inBoxType);

        // 自定义文件夹路径
        String path = (String) params.get("path");
        if (path != null) searchParams.put("path", path);

        // 分组、排序
        searchParams.put("groupType", params.getOrDefault("groupType", "all"));
        searchParams.put("orderType", params.get("orderType"));
        searchParams.put("orderRule", Objects.toString(params.get("orderRule"), "DESC"));

        PageResult<EmailAffair> result;
        switch (type != null ? type : "inBox") {
            case "draft":
                result = emailService.findDraft(pageNo, pageSize, userId, searchParams); break;
            case "sent":
                result = emailService.findSent(pageNo, pageSize, userId, searchParams); break;
            case "delete":
                result = emailService.findDeleted(pageNo, pageSize, userId, searchParams); break;
            case "collection":
                result = emailService.findCollect(pageNo, pageSize, userId, searchParams); break;
            case "encryption":
                result = emailService.findSecret(pageNo, pageSize, userId, searchParams); break;
            default:
                if (path != null) {
                    result = emailService.findByPath(pageNo, pageSize, userId, path, searchParams);
                } else {
                    result = emailService.findInbox(pageNo, pageSize, userId, searchParams);
                }
        }

        // 为每个 affair 获取密级信息
        enrichSecretInfo(result.getRecords());

        return R.ok(result);
    }

    // ==================== 详情 ====================

    @GetMapping("/content/{affairId}")
    public R<Map<String, Object>> getContent(@PathVariable Long affairId) {
        EmailAffair affair = emailService.getAffairById(affairId);
        if (affair == null) {
            return R.fail("邮件不存在，可能已被撤回或删除");
        }

        EmailSummary summary = emailService.getSummaryById(affair.getSummaryId());
        if (summary == null) {
            return R.fail("邮件不存在");
        }

        // 标记已读
        affair.setReadFlag(1);
        affair.setBrowseTime(java.time.LocalDateTime.now());
        emailService.getAffairById(affairId); // will be updated via service

        // 组装返回数据，兼容原 OA 前端 MobileInMailPo 结构
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", affair.getId());
        data.put("summaryId", summary.getId());
        data.put("subject", summary.getSubject());
        data.put("senderName", summary.getSenderName());
        data.put("senderId", summary.getSenderId());
        data.put("sendTo", summary.getSendTo());
        data.put("copyTo", summary.getCopyTo());
        data.put("content", summary.getSummaryContent());
        data.put("createTime", summary.getCreateTime());
        data.put("attachmentsFlag", summary.getAttachmentsFlag());
        data.put("state", affair.getState());
        data.put("readFlag", affair.getReadFlag());
        data.put("isHandled", affair.getIsHandled());
        data.put("collect", affair.getCollect());
        data.put("mark", summary.getMark());
        data.put("secretIdStr", affair.getSecretIdStr());
        data.put("secretNameStr", affair.getSecretNameStr());
        data.put("passTheAudit", affair.getPassTheAudit());
        data.put("kuaWang", summary.getKuaWang());
        data.put("approverStr", summary.getApproverStr());
        data.put("attachments", summary.getAttachments());

        // 获取附件列表
        data.put("attachmentList", attachmentService.listBySummaryId(summary.getId()));

        return R.ok(data);
    }

    // ==================== 发送/保存 ====================

    @PostMapping("/send")
    public R<Map<String, Object>> sendEmail(@RequestBody Map<String, Object> params) {
        String type = (String) params.getOrDefault("type", "send");

        if ("draft".equals(type) || Boolean.TRUE.equals(params.get("timedTask"))) {
            EmailSummary draft = emailService.saveDraft(params);
            Map<String, Object> result = new HashMap<>();
            result.put("summaryId", draft.getId());
            result.put("affairId", draft.getId());
            return R.ok(result);
        }

        EmailSummary summary = emailService.sendEmail(params);
        Map<String, Object> result = new HashMap<>();
        result.put("summaryId", summary.getId());
        return R.ok(result);
    }

    // ==================== 操作 ====================

    @PostMapping("/delete")
    public R<Void> deleteMail(@RequestBody Map<String, String> params) {
        String affairId = params.get("affairId");
        String type = params.get("type");
        if (affairId == null || type == null) {
            return R.fail("参数缺失");
        }
        List<Long> ids = parseIdList(affairId);
        emailService.deleteMail(ids, type);
        return R.ok();
    }

    @PostMapping("/recall")
    public R<String> recallMail(@RequestBody Map<String, String> params) {
        String affairIds = params.get("affairIds");
        if (affairIds == null) return R.fail("参数缺失");

        List<Long> ids = parseIdList(affairIds);
        String failed = emailService.recallMail(ids);
        if (failed.isEmpty()) {
            return R.ok("邮件撤销成功");
        }
        return R.ok("部分撤销失败：" + failed);
    }

    @PostMapping("/recovery")
    public R<Void> recovery(@RequestBody Map<String, String> params) {
        String affairIds = params.get("affairIds");
        if (affairIds == null) return R.fail("参数缺失");

        emailService.recoveryMail(parseIdList(affairIds));
        return R.ok();
    }

    @PostMapping("/mark")
    public R<String> mark(@RequestBody Map<String, String> params) {
        String affairId = params.get("affairId");
        String flagType = params.get("flagType");
        if (affairId == null || flagType == null) return R.fail("参数缺失");

        String msg = emailService.markAffairs(parseIdList(affairId), flagType);
        return R.ok(msg);
    }

    @PostMapping("/tagUnRead")
    public R<Void> tagUnRead(@RequestBody Map<String, String> params) {
        String affairId = params.get("affairId");
        if (affairId == null) return R.fail("参数缺失");

        emailService.markUnread(Long.parseLong(affairId));
        return R.ok();
    }

    // ==================== 数量 ====================

    @GetMapping("/counts")
    public R<Map<String, Object>> counts() {
        Long userId = UserContextHolder.getUserId();
        Map<String, Integer> counts = emailService.countGroupByState(userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("inBox", counts.getOrDefault("inbox", 0));
        result.put("draft", counts.getOrDefault("draft", 0));
        result.put("sent", counts.getOrDefault("sent", 0));
        result.put("delete", counts.getOrDefault("deleted", 0));
        result.put("encryption", counts.getOrDefault("encryption", 0));
        result.put("collect", counts.getOrDefault("collect", 0));
        return R.ok(result);
    }

    // ==================== 回复/转发/编辑 ====================

    @PostMapping("/reply")
    public R<Map<String, Object>> reply(@RequestBody Map<String, Object> params) {
        return R.ok(emailService.getReplyData(params));
    }

    @PostMapping("/allReply")
    public R<Map<String, Object>> allReply(@RequestBody Map<String, Object> params) {
        return R.ok(emailService.getAllReplyData(params));
    }

    @PostMapping("/forward")
    public R<Map<String, Object>> forward(@RequestBody Map<String, Object> params) {
        return R.ok(emailService.getForwardData(params));
    }

    @PostMapping("/compile")
    public R<Map<String, Object>> compile(@RequestBody Map<String, Object> params) {
        return R.ok(emailService.getInternalCompileData(params));
    }

    // ==================== 回执/导出 ====================

    @PostMapping("/checkStatus")
    public R<List<Map<String, Object>>> checkStatus(@RequestBody Map<String, String> params) {
        String summaryId = params.get("summaryId");
        String affairId = params.get("affairId");

        List<Map<String, Object>> list = new ArrayList<>();
        List<EmailAffair> affairs;

        if (summaryId != null) {
            affairs = emailService.getAffairsBySummaryId(Long.parseLong(summaryId));
        } else if (affairId != null) {
            EmailAffair a = emailService.getAffairById(Long.parseLong(affairId));
            affairs = a != null ? emailService.getAffairsBySummaryId(a.getSummaryId()) : Collections.emptyList();
        } else {
            return R.fail("参数缺失");
        }

        for (EmailAffair a : affairs) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("memberId", a.getMemberId());
            m.put("memberName", a.getMemberName());
            m.put("readFlag", a.getReadFlag());
            m.put("browseTime", a.getBrowseTime());
            list.add(m);
        }
        return R.ok(list);
    }

    @PostMapping("/export")
    public R<Map<String, String>> exportEmail(@RequestBody Map<String, String> params) {
        // EML 导出需要额外处理，暂返回附件下载信息
        return R.fail("导出功能开发中");
    }

    // ==================== 搜索人员 ====================

    @PostMapping("/searchMember")
    public R<List<Map<String, Object>>> searchMember(@RequestBody Map<String, String> params) {
        String keyword = params.get("keyword");
        if (keyword == null || keyword.length() < 1) return R.ok(Collections.emptyList());

        // 此处简化，实际需要调用 OrgMemberService
        return R.ok(Collections.emptyList());
    }

    // ==================== 辅助 ====================

    @DeleteMapping("/cancelAutosave")
    public R<Void> cancelAutosave(@RequestParam String firstAutosaveTime,
                                  @RequestParam String oldAffairId) {
        Map<String, Object> params = new HashMap<>();
        params.put("firstAutosaveTime", firstAutosaveTime);
        params.put("oldAffairId", oldAffairId);
        // cancelAutosave 在 sendEmail 内部会自动调用
        return R.ok();
    }

    // ==================== 私有工具方法 ====================

    private Map<String, Object> parseSearchParams(Map<String, Object> params) {
        Map<String, Object> sp = new HashMap<>();
        String searchWord = (String) params.get("searchWord");
        if (searchWord != null && !searchWord.isEmpty()) {
            try {
                Map<String, Object> map = com.alibaba.fastjson.JSON.parseObject(searchWord);
                for (Map.Entry<String, Object> e : map.entrySet()) {
                    switch (e.getKey()) {
                        case "0": sp.put("subject", e.getValue()); sp.put("sendtoName", e.getValue());
                                  sp.put("senderName", e.getValue()); sp.put("contentName", e.getValue());
                                  sp.put("attachName", e.getValue()); sp.put("flag", "selectAll"); break;
                        case "1": sp.put("subject", e.getValue()); break;
                        case "2": sp.put("createDate", e.getValue()); break;
                        case "3": sp.put("sendtoName", e.getValue()); break;
                        case "4": sp.put("copytoName", e.getValue()); break;
                        case "5": sp.put("senderName", e.getValue()); break;
                        case "6": sp.put("contentName", e.getValue()); break;
                        case "7": sp.put("attachName", e.getValue()); break;
                    }
                }
            } catch (Exception ignored) {}
        }
        return sp;
    }

    private List<Long> parseIdList(String commaSep) {
        List<Long> ids = new ArrayList<>();
        for (String s : commaSep.split(",")) {
            s = s.trim();
            if (!s.isEmpty()) ids.add(Long.parseLong(s));
        }
        return ids;
    }

    /** 为每个 affair 补充密级信息 */
    private void enrichSecretInfo(List<EmailAffair> list) {
        // TODO: 从 email_approval 表中查询密级
    }
}
