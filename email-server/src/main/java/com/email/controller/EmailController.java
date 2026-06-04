package com.email.controller;

import com.email.common.R;
import com.email.entity.*;
import com.email.manager.InternalMailManager;
import com.email.platform.*;
import com.email.platform.entity.OrgMember;
import com.email.security.UserContextHolder;
import com.email.service.NewEmailUtils;
import com.email.util.InMailUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;

/**
 * 邮件 REST API —— 从 OA EmailPCResource 迁移为 Spring MVC。
 * 所有 API 签名和行为与 OA 保持一致。
 */

@RestController
@RequestMapping("/email")
public class EmailController {

    @Resource(name = "internalMailManager")
    private InternalMailManager mgr;

    // ==================== 列表 ====================

    @PostMapping("/list")
    public R<Map<String, Object>> list(@RequestBody Map<String, Object> params) {
        Long userId = UserContextHolder.getUserId();
        Map<String, Object> result = new LinkedHashMap<>();

        String type = Objects.toString(params.get("type"), "inBox");
        int pageNo = params.get("pageNo") != null ? (Integer) params.get("pageNo") : 1;
        int pageSize = params.get("pageSize") != null ? (Integer) params.get("pageSize") : 15;

        FlipInfo fi = new FlipInfo();
        fi.setPage(pageNo);
        fi.setSize(pageSize);
        fi.setNeedTotal(true);

        Map<String, Object> p = new HashMap<>();
        p.put("readType", params.get("inBoxType"));
        p.put("path", params.get("path"));
        p.put("groupType", params.getOrDefault("groupType", "all"));
        mergeSearchParams(p, params);

        switch (type) {
            case "draft":  mgr.findDraftAffair(fi, userId, p); break;
            case "sent":   mgr.findSentAffair(fi, userId, p); break;
            case "delete": mgr.findDeleteAffair(fi, userId, p); break;
            case "collection": mgr.findCollectAffair(fi, userId, p); break;
            case "encryption": mgr.findInboxAffairBySecret(fi, p); break;
            default:       mgr.findInboxAffair(fi, userId, p); break;
        }

        result.put("code", "00010001");
        result.put("records", fi.getData());
        result.put("total", fi.getTotal());
        result.put("current", fi.getPage());
        result.put("size", fi.getSize());
        result.put("pages", fi.getPages());
        return R.ok(result);
    }

    // ==================== 详情 ====================

    @GetMapping("/content/{affairId}")
    public R<Map<String, Object>> content(@PathVariable Long affairId) {
        InMailAffair affair = mgr.getInMailAffairById(affairId);
        if (affair == null) return R.fail("邮件不存在，可能已被撤回或删除");

        InMailSummary s = mgr.getInMailSummaryById(affair.getObjectId());
        if (s == null) return R.fail("邮件不存在");

        // 标记已读
        affair.setReadFlag(true);
        affair.setBrowseTime(new Timestamp(System.currentTimeMillis()));
        mgr.updateInMailAffair(affair);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", affair.getId());
        data.put("summaryId", s.getId());
        data.put("subject", s.getSubject());
        data.put("senderName", getMemberName(s.getStartMemberId()));
        data.put("senderId", s.getStartMemberId());
        data.put("sendTo", s.getSendTo());
        data.put("copyTo", s.getCopyTo());
        data.put("content", s.getContent());
        data.put("createTime", s.getCreateDate());
        data.put("attachmentsFlag", s.getAttachmentsFlag());
        data.put("state", affair.getState());
        data.put("readFlag", affair.getReadFlag());
        data.put("isHandled", affair.getIsHandled());
        data.put("collect", affair.getCollect());
        data.put("mark", s.getMark());
        data.put("passTheAudit", affair.getPassTheAudit());
        data.put("kuaWang", s.getKuaWang());
        data.put("approverStr", s.getApproverStr());
        data.put("attachments", s.getAttachments());

        return R.ok(data);
    }

    // ==================== 发送/草稿 ====================

    @PostMapping("/send")
    public R<Map<String, Object>> send(@RequestBody Map<String, Object> params) {
        String type = Objects.toString(params.get("type"), "send");
        Map<String, Object> result;

        if ("draft".equals(type) || Boolean.TRUE.equals(params.get("timedTask"))) {
            result = NewEmailUtils.save(params, mgr);
        } else {
            result = NewEmailUtils.sendEmail(params, mgr);
        }

        if ("10001".equals(result.get("code"))) {
            return R.ok(result);
        }
        return R.fail(500, (String) result.getOrDefault("msg", "发送失败"));
    }

    // ==================== 操作 ====================

    @PostMapping("/delete")
    public R<Void> delete(@RequestBody Map<String, String> params) {
        String affairId = params.get("affairId");
        String type = params.get("type");
        if (affairId == null || type == null) return R.fail("参数缺失");

        for (String id : affairId.split(",")) {
            mgr.deleteAffair(type, id.trim());
        }
        return R.ok();
    }

    @PostMapping("/recall")
    public R<String> recall(@RequestBody Map<String, String> params) {
        String affairIds = params.get("affairIds");
        if (affairIds == null) return R.fail("参数缺失");
        // 撤销：改状态为草稿
        mgr.updateFieldById("STATE", 1, parseIds(affairIds), null);
        return R.ok("撤销成功");
    }

    @PostMapping("/recovery")
    public R<Void> recovery(@RequestBody Map<String, String> params) {
        String affairIds = params.get("affairIds");
        if (affairIds == null) return R.fail("参数缺失");
        for (String s : affairIds.split(",")) {
            mgr.recoveryAffair("delete", s.trim());
        }
        return R.ok();
    }

    @PostMapping("/mark")
    public R<String> mark(@RequestBody Map<String, String> params) {
        String affairId = params.get("affairId");
        String flagType = params.get("flagType");
        if (affairId == null || flagType == null) return R.fail("参数缺失");

        boolean ok = mgr.updateAllAffairIsHandle(parseIds(affairId), flagType);
        if (ok) return R.ok("操作成功");
        return R.fail("操作失败");
    }

    @PostMapping("/tagUnRead")
    public R<Void> tagUnRead(@RequestBody Map<String, String> params) {
        String affairId = params.get("affairId");
        if (affairId == null) return R.fail("参数缺失");
        mgr.updateFieldById("READ_FLAG", 0, parseIds(affairId), null);
        return R.ok();
    }

    // ==================== 数量 ====================

    @GetMapping("/counts")
    public R<Map<String, Object>> counts() {
        Long userId = UserContextHolder.getUserId();
        Map<String, Object> p = new HashMap<>(); p.put("memberId", userId);
        int inbox = mgr.findInboxNextAffair(p);

        // 聚合统计各种状态
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("inBox", inbox);
        result.put("draft", countByState(1, userId));
        result.put("sent", countByState(0, userId));
        result.put("delete", countByState(3, userId));
        result.put("encryption", countByState(5, userId));
        result.put("collect", countCollect(userId));
        return R.ok(result);
    }

    // ==================== 回复/转发/编辑 ====================

    @PostMapping("/reply")
    public R<Map<String, Object>> reply(@RequestBody Map<String, Object> params) {
        return R.ok(NewEmailUtils.reply(params, mgr));
    }

    @PostMapping("/allReply")
    public R<Map<String, Object>> allReply(@RequestBody Map<String, Object> params) {
        return R.ok(NewEmailUtils.allReply(params, mgr));
    }

    @PostMapping("/forward")
    public R<Map<String, Object>> forward(@RequestBody Map<String, Object> params) {
        return R.ok(NewEmailUtils.forwordMail(params, mgr));
    }

    @PostMapping("/compile")
    public R<Map<String, Object>> compile(@RequestBody Map<String, Object> params) {
        return R.ok(NewEmailUtils.internalCompile(params, mgr));
    }

    // ==================== 回执 ====================

    @PostMapping("/checkStatus")
    public R<List<Map<String, Object>>> checkStatus(@RequestBody Map<String, String> params) {
        String summaryId = params.get("summaryId");
        List<Map<String, Object>> list = new ArrayList<>();
        if (summaryId != null) {
            List<InMailAffair> affairs = mgr.findAffairByInMailId(Long.parseLong(summaryId));
            for (InMailAffair a : affairs) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("memberId", a.getMemberId());
                m.put("memberName", getMemberName(a.getMemberId()));
                m.put("readFlag", a.getReadFlag());
                m.put("browseTime", a.getBrowseTime());
                list.add(m);
            }
        }
        return R.ok(list);
    }

    // ==================== 加密 ====================

    @PostMapping("/encryption")
    public R<Void> encryption(@RequestBody Map<String, String> params) {
        // TODO: 验证加密箱密码
        return R.ok();
    }

    @PostMapping("/updateOrAddPwd")
    public R<String> updateOrAddPwd(@RequestBody Map<String, String> params) {
        String pwd = params.get("pwd");
        Long userId = UserContextHolder.getUserId();
        if (pwd != null) {
            InMailSecret s = new InMailSecret();
            s.setMemberId(userId);
            s.setPwd(InMailUtil.secretPwd(UserContextHolder.get().getLoginName(), pwd));
            mgr.secretTool(s, userId, true);
        }
        return R.ok("密码已设置");
    }

    // ==================== 搜索人员 ====================

    @PostMapping("/seaechName")
    public R<List<Map<String, Object>>> searchMember(@RequestBody Map<String, String> params) {
        String key = params.get("keyword");
        if (key == null || key.isEmpty()) return R.ok(Collections.emptyList());
        List<Map<String, Object>> list = mgr.getSearchDataStr(key, "1");
        return R.ok(list != null ? list : Collections.emptyList());
    }

    // ==================== 其他 ====================

    @GetMapping("/cancelAutosave")
    public R<Void> cancelAutosave(@RequestParam String firstAutosaveTime,
                                   @RequestParam String oldAffairId) {
        mgr.cancelOrdeleteAutosaveState(true, new Timestamp(Long.parseLong(firstAutosaveTime)));
        return R.ok();
    }

    @PostMapping("/preview")
    public R<Map<String, String>> preview(@RequestBody Map<String, String> params) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("url", params.getOrDefault("fileId", ""));
        return R.ok(m);
    }

    @PostMapping("/mailSignature")
    public R<String> mailSignature() {
        return R.ok(InMailUtil.mailSignature());
    }

    @PostMapping("/showKuaWang")
    public R<Boolean> showKuaWang() { return R.ok(false); }

    @PostMapping("/getSecretList")
    public R<List<Object>> getSecretList() { return R.ok(Collections.emptyList()); }

    @GetMapping("/getUUID")
    public R<String> getUUID() { return R.ok(String.valueOf(UUIDLong.longValue())); }

    // ==================== 内部辅助 ====================

    private void mergeSearchParams(Map<String, Object> p, Map<String, Object> params) {
        String searchWord = (String) params.get("searchWord");
        if (searchWord == null || searchWord.isEmpty()) return;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = com.alibaba.fastjson.JSON.parseObject(searchWord, Map.class);
            for (Map.Entry<String, Object> e : map.entrySet()) {
                switch (e.getKey()) {
                    case "0": p.put("subject", e.getValue()); p.put("senderName", e.getValue()); break;
                    case "1": p.put("subject", e.getValue()); break;
                    case "3": p.put("sendtoName", e.getValue()); break;
                    case "5": p.put("senderName", e.getValue()); break;
                    case "6": p.put("contentName", e.getValue()); break;
                    case "7": p.put("attachName", e.getValue()); break;
                }
            }
        } catch (Exception ignored) {}
    }

    private List<Long> parseIds(String csv) {
        List<Long> ids = new ArrayList<>();
        for (String s : csv.split(",")) {
            s = s.trim();
            if (!s.isEmpty()) ids.add(Long.parseLong(s));
        }
        return ids;
    }

    private String getMemberName(Long id) {
        OrgMember m = DBAgent.get(OrgMember.class, id);
        return m != null ? m.getName() : "";
    }

    private int countByState(int state, Long userId) {
        Map<String, Object> p = new HashMap<>();
        p.put("mid", userId);
        p.put("state", state);
        return (int) DBAgent.count("FROM InMailAffair WHERE senderId=:mid AND memberId=:mid AND state=:state AND delFlag=0", p);
    }

    private int countCollect(Long userId) {
        Map<String, Object> p = new HashMap<>();
        p.put("mid", userId);
        return (int) DBAgent.count("FROM InMailAffair WHERE memberId=:mid AND collect=1 AND state!=3 AND delFlag=0", p);
    }
}
