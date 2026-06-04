package com.email.service;

import com.email.constants.InMailConstant;
import com.email.entity.*;
import com.email.manager.InternalMailManager;
import com.email.platform.*;
import com.email.platform.entity.OrgMember;
import com.email.security.UserInfo;
import com.email.util.InMailUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.*;

/**
 * 邮件发送/保存工具 —— 从 OA NewEmailUtils 提取核心逻辑（去掉跨网/审批/Office转换等功能）。
 * 方法签名和业务逻辑与 OA 保持一致。
 */
public class NewEmailUtils {

    private static final Log log = LogFactory.getLog(NewEmailUtils.class);

    /**
     * 发送邮件 — OA NewEmailUtils.sendEmail 核心逻辑提取。
     */
    public static Map<String, Object> sendEmail(Map<String, Object> param,
                                                 InternalMailManager mmgr) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 获取当前用户
        UserInfo user = AppContext.getCurrentUser();
        if (user == null) {
            String uid = (String) param.get("sendUserId");
            if (Strings.isNotBlank(uid)) {
                OrgMember mem = DBAgent.get(OrgMember.class, Long.parseLong(uid));
                if (mem != null) {
                    user = new UserInfo();
                    user.setId(mem.getId());
                    user.setName(mem.getName());
                    user.setLoginName(mem.getLoginName());
                    user.setDepartmentId(mem.getDepartmentId());
                    user.setLoginAccount(mem.getAccountId());
                }
            }
        }
        if (user == null) {
            result.put("code", "10003");
            result.put("msg", "邮件发送失败：发起人不能为空！");
            return result;
        }

        // 2. 解析参数
        String subject = Objects.toString(param.get("subject"), "");
        String text = Objects.toString(param.get("content"), "");
        String to = Objects.toString(param.get("receiversStr"), "");
        String toIds = Objects.toString(param.get("receivers"), "");
        String cc = Objects.toString(param.get("copyReceiversStr"), "");
        String ccIds = Objects.toString(param.get("copyReceivers"), "");
        String mark = Objects.toString(param.get("mark"), null);
        String summaryId = Objects.toString(param.get("summaryId"), null);
        String affairId = Objects.toString(param.get("affairId"), null);
        String isCross = Objects.toString(param.get("kuaWang"), "false");
        String type = Objects.toString(param.get("type"), "send");
        String attachmentsStr = Objects.toString(param.get("attachments"), null);
        String shortMsg = Objects.toString(param.get("shortMessage"), null);

        // 3. 获取或创建邮件主体
        InMailSummary bean = null;
        boolean isExist = false;
        if (Strings.isNotBlank(summaryId)) {
            bean = mmgr.getInMailSummaryById(Long.parseLong(summaryId));
        }
        if (bean == null && Strings.isNotBlank(affairId)) {
            InMailAffair aff = mmgr.getInMailAffairById(Long.parseLong(affairId));
            if (aff != null) bean = mmgr.getInMailSummaryById(aff.getObjectId());
        }
        if (bean != null) {
            isExist = true;
        } else {
            bean = new InMailSummary();
        }

        // 4. 处理回复/转发标记
        if (Strings.isNotBlank(mark)) {
            bean.setMark(mark);
            if ("forwordMail".equals(mark)) {
                String pid = (String) param.get("parentformSummaryid");
                if (Strings.isNotBlank(pid)) bean.setParentformSummaryid(Long.parseLong(pid));
                String fid = (String) param.get("forwardMemberId");
                if (Strings.isNotBlank(fid)) bean.setForwardMemberId(Long.parseLong(fid));
                String fm = (String) param.get("forwardMember");
                if (Strings.isNotBlank(fm)) bean.setForwardMember(fm);
                String paid = (String) param.get("parentformAffairid");
                if (Strings.isNotBlank(paid)) {
                    bean.setParentformAffairid(Long.parseLong(paid));
                    InMailAffair aff = mmgr.getInMailAffairById(Long.parseLong(paid));
                    if (aff != null) { aff.setIsForward(true); aff.setForwardMember(fm); mmgr.updateInMailAffair(aff); }
                }
            } else if ("reply".equals(mark) || "allReply".equals(mark)) {
                String rpid = (String) param.get("replyParentSummaryid");
                if (Strings.isNotBlank(rpid)) bean.setReplyParentSummaryid(Long.parseLong(rpid));
                String rmid = (String) param.get("replyMemberId");
                if (Strings.isNotBlank(rmid)) bean.setReplyMemberId(Long.parseLong(rmid));
                String rm = (String) param.get("replyMember");
                if (Strings.isNotBlank(rm)) bean.setReplyMember(rm);
                String rpaid = (String) param.get("replyParentAffairid");
                if (Strings.isNotBlank(rpaid)) {
                    bean.setReplyParentAffairid(Long.parseLong(rpaid));
                    InMailAffair aff = mmgr.getInMailAffairById(Long.parseLong(rpaid));
                    if (aff != null) {
                        aff.setIsReply(true);
                        aff.setReplyType("allReply".equals(mark) ? InMailConstant.InMailReplyType.all.ordinal() : InMailConstant.InMailReplyType.reply.ordinal());
                        mmgr.updateInMailAffair(aff);
                    }
                }
            }
        }

        if ("true".equals(isCross)) bean.setKuaWang(true);
        bean.setApproverStr(Objects.toString(param.get("approverStr"), null));
        bean.setApprover(Objects.toString(param.get("approver"), null));

        // 5. 组装邮件主体（OA InMailUtil.makeInMailSummary）
        InMailUtil.makeInMailSummary(bean, user, subject, to, toIds, cc, ccIds, text, shortMsg);

        // 6. 解析收件人/抄送人 → 展开为成员列表
        Set<OrgMember> memberSet = new LinkedHashSet<>();
        OrgService.resolveMemberSet(memberSet, toIds);
        OrgService.resolveMemberSet(memberSet, ccIds);

        // 7. 组装事项
        List<InMailAffair> affairList = new ArrayList<>();
        boolean isDraft = "draft".equals(type);

        // 发件人自己的事务
        InMailAffair sentAffair = new InMailAffair();
        InMailUtil.makeInMailAffair(sentAffair, bean, user, subject,
                isDraft ? InMailConstant.InMailAffairState.draf.ordinal() : InMailConstant.InMailAffairState.sent.ordinal(), user.getId());
        affairList.add(sentAffair);

        // 每个收件人一条事务
        for (OrgMember m : memberSet) {
            if (m.getId().equals(user.getId())) continue; // 跳过自己
            InMailAffair a = new InMailAffair();
            InMailUtil.makeInMailAffair(a, bean, user, subject,
                    isDraft ? InMailConstant.InMailAffairState.draf.ordinal() : InMailConstant.InMailAffairState.run.ordinal(), m.getId());
            affairList.add(a);
        }

        // 8. 计算邮件大小
        long size = 0L;
        if (Strings.isNotBlank(text)) {
            String tmpPath = System.getProperty("java.io.tmpdir") + File.separator + "email_" + bean.getId() + ".html";
            try (FileOutputStream fos = new FileOutputStream(tmpPath)) {
                byte[] b = text.getBytes("UTF-8");
                fos.write(b);
                size += b.length;
            } catch (Exception ignore) {}
            new File(tmpPath).delete();
        }
        bean.setSize(size);

        // 9. 保存
        mmgr.saveInMailSummary(bean, affairList, isExist);

        log.info("邮件" + (isDraft ? "保存草稿" : "发送") + "成功: id=" + bean.getId() + ", subject=" + subject);

        result.put("code", "10001");
        result.put("summaryId", bean.getId());
        if (!affairList.isEmpty()) result.put("affairId", affairList.get(0).getId());
        return result;
    }

    /**
     * 保存草稿
     */
    public static Map<String, Object> save(Map<String, Object> param, InternalMailManager mmgr) {
        param.put("type", "draft");
        return sendEmail(param, mmgr);
    }

    /**
     * 获取编辑/草稿数据 — OA NewEmailUtils.internalCompile
     */
    public static Map<String, Object> internalCompile(Map<String, Object> param, InternalMailManager mmgr) {
        String mark = (String) param.get("mark");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("mark", "new");

        if ("reEdit".equals(mark)) {
            String sid = (String) param.get("summaryId");
            if (Strings.isNotBlank(sid)) {
                InMailSummary s = mmgr.getInMailSummaryById(Long.parseLong(sid));
                if (s != null) {
                    data.put("mark", "reEdit");
                    data.put("subject", s.getSubject());
                    data.put("sendTo", s.getSendTo());
                    data.put("sendToIds", s.getSendToIds());
                    data.put("copyTo", s.getCopyTo());
                    data.put("copyToIds", s.getCopyToIds());
                    data.put("content", s.getContent());
                    data.put("summaryId", sid);
                }
            }
        }
        return data;
    }

    /** 回复数据 */
    public static Map<String, Object> reply(Map<String, Object> param, InternalMailManager mmgr) {
        String sid = (String) param.get("summaryId");
        String aid = (String) param.get("affairId");
        Map<String, Object> data = new LinkedHashMap<>();

        if (Strings.isNotBlank(sid)) {
            InMailSummary s = mmgr.getInMailSummaryById(Long.parseLong(sid));
            if (s != null) {
                OrgMember sender = DBAgent.get(OrgMember.class, s.getStartMemberId());
                data.put("subject", "回复：" + s.getSubject());
                data.put("sendTo", sender != null ? sender.getName() : "");
                data.put("sendToIds", "Member|" + s.getStartMemberId());
                data.put("content", s.getContent());
                data.put("replyParentSummaryId", sid);
                data.put("replyParentAffairId", aid);
                data.put("mark", "reply");
                data.put("summaryId", sid);
                data.put("affairId", aid);
            }
        }
        return data;
    }

    /** 全部回复 */
    public static Map<String, Object> allReply(Map<String, Object> param, InternalMailManager mmgr) {
        Map<String, Object> data = reply(param, mmgr);
        data.put("mark", "allReply");
        return data;
    }

    /** 转发数据 */
    public static Map<String, Object> forward(Map<String, Object> param, InternalMailManager mmgr) {
        String sid = (String) param.get("summaryId");
        Map<String, Object> data = new LinkedHashMap<>();
        if (Strings.isNotBlank(sid)) {
            InMailSummary s = mmgr.getInMailSummaryById(Long.parseLong(sid));
            if (s != null) {
                data.put("subject", "转发：" + s.getSubject());
                data.put("content", s.getContent());
                data.put("parentformSummaryId", sid);
                data.put("mark", "forward");
                data.put("summaryId", sid);
            }
        }
        return data;
    }
}
