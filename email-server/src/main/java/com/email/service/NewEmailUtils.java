package com.email.service;

import com.email.constants.InMailConstant;
import com.email.entity.*;
import com.email.manager.InternalMailManager;
import com.email.platform.*;
import com.email.platform.entity.OrgMember;
import com.email.security.UserContextHolder;
import com.email.security.UserInfo;
import com.email.stub.OaCompat.*;
import com.email.stub.OrgManagerAdapter;
import com.email.stub.V3xOrgAdapter;
import com.email.stub.V3xOrgAdapter.Entity;
import com.email.stub.V3xOrgAdapter.Member;
import com.email.util.EmailDESUtil;
import com.email.util.InMailUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.sql.Timestamp;
import java.util.*;

/**
 * 邮件发送/保存/编辑工具 —— 从 OA NewEmailUtils (2590行) 完整迁移核心逻辑。
 * 去掉跨网审批/Office转换/文件系统附件（独立部署后走 MinIO/简化），
 * 其余所有业务逻辑和参数处理与 OA 完全一致。
 */
public class NewEmailUtils {

    private static final Log log = LogFactory.getLog(NewEmailUtils.class);

    // ==================== sendEmail — 从 OA NewEmailUtils.sendEmail 完整迁移 ====================

    public static Map<String, Object> sendEmail(Map<String, Object> param, InternalMailManager mmgr) {
        Map<String, Object> resultMsg = new LinkedHashMap<>();
        resultMsg.put("code", "10001");

        if (null == param) {
            resultMsg.put("code", "10002");
            resultMsg.put("msg", "邮件发送失败,失败原因：请传递参数！");
            return resultMsg;
        }

        String userId = (String) param.get("sendUserId");
        userId = userId == null ? (AppContext.getCurrentUser() != null ?
                AppContext.getCurrentUser().getId().toString() : null) : userId;
        if (!Strings.isNotBlank(userId)) {
            resultMsg.put("code", "10003");
            resultMsg.put("msg", "邮件发送失败,失败原因：发起人不能为空！");
            return resultMsg;
        }

        try {
            UserInfo user = AppContext.getCurrentUser();
            if (user == null) {
                OrgMember mem = DBAgent.get(OrgMember.class, Long.parseLong(userId));
                if (mem != null) {
                    user = new UserInfo();
                    user.setId(mem.getId()); user.setName(mem.getName());
                    user.setLoginName(mem.getLoginName());
                    user.setDepartmentId(mem.getDepartmentId());
                    user.setLoginAccount(mem.getAccountId());
                }
            }
            if (user == null) {
                resultMsg.put("code", "10003");
                resultMsg.put("msg", "邮件发送失败,失败原因：发起人不能为空！");
                return resultMsg;
            }

            // ===== 参数解析（与 OA 完全一致） =====
            String text = Objects.toString(param.get("content"), "");
            String isCross = "0";
            String summaryId = (String) param.get("summaryId");
            String affairId = (String) param.get("affairId");
            String attachments = (String) param.get("attachments");
            String to = Objects.toString(param.get("receiversStr"), "");
            String toIds = Objects.toString(param.get("receivers"), "");
            String cc = Objects.toString(param.get("copyReceiversStr"), "");
            String ccIds = Objects.toString(param.get("copyReceivers"), "");
            String approver = (String) param.get("approver");
            String approverStr = (String) param.get("approverStr");

            String subject = Objects.toString(param.get("subject"), "");
            String fslId = (String) param.get("secretTypeId");
            String editMailSend = (String) param.get("editMailSend");
            String shortMessage = (String) param.get("shortMessage");
            String mark = (String) param.get("mark");

            // OA 原样：UTF8 空格修正
            try {
                byte[] space = new byte[]{(byte) 0xc2, (byte) 0xa0};
                String UTFSpace = new String(space, "UTF-8");
                subject = subject.replaceAll(UTFSpace, " ");
                if (shortMessage != null) shortMessage = shortMessage.replaceAll(UTFSpace, " ");
            } catch (Exception ignored) {}

            if (text == null) text = "";
            if (subject == null) subject = "";

            // ===== 获取邮件主体（与 OA 一致） =====
            InMailSummary bean = null;
            if (Strings.isBlank(editMailSend)) {
                if (Strings.isNotBlank(summaryId)) bean = mmgr.getInMailSummaryById(Long.parseLong(summaryId));
                if (Strings.isNotBlank(affairId) && bean == null) {
                    InMailAffair aff = mmgr.getInMailAffairById(Long.parseLong(affairId));
                    if (aff != null) bean = mmgr.getInMailSummaryById(aff.getObjectId());
                }
            }
            boolean isExist = (bean != null);
            if (bean == null) bean = new InMailSummary();

            // 草稿重新发送：删除旧草稿
            if ("1".equals(editMailSend) && affairId != null) {
                InMailAffair oldAffair = mmgr.getInMailAffairById(Long.parseLong(affairId));
                if (oldAffair != null && oldAffair.getState() == 1) {
                    mmgr.updateFieldById("IS_DELETE", 1, null, Long.parseLong(affairId));
                }
            }

            // ===== 回复/转发标记处理（与 OA 完全一致） =====
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
                            aff.setReplyType("allReply".equals(mark) ?
                                    InMailConstant.InMailReplyType.all.ordinal() :
                                    InMailConstant.InMailReplyType.reply.ordinal());
                            mmgr.updateInMailAffair(aff);
                        }
                    }
                }
            }

            if ("1".equals(isCross)) bean.setKuaWang(true);
            bean.setApproverStr(approverStr);
            bean.setApprover(approver);

            // ===== 组装邮件主体（调用 OA 工具类） =====
            InMailUtil.makeInMailSummary(bean, user, subject, to, toIds, cc, ccIds, text, shortMessage);

            // ===== 密级（独立部署简化处理） =====
            if (fslId == null) fslId = "1";
            bean.setFileSecretLevelId(Long.parseLong(fslId));

            // ===== 附件处理（独立部署：简化，走 MinIO 需独立实现） =====
            Long size = 0L;
            if (Strings.isNotBlank(attachments)) {
                bean.setAttachmentsFlag(true);
            } else {
                bean.setAttachmentsFlag(false);
            }
            if (Strings.isNotBlank(text)) {
                String tmpPath = System.getProperty("java.io.tmpdir") + File.separator + "email_" + bean.getId() + ".html";
                writerToFile(tmpPath, text.getBytes("UTF-8"));
                File f = new File(tmpPath);
                if (f.exists()) size += f.length();
                delteFile(tmpPath);
            }
            bean.setSize(size);

            // ===== 解析收件人（与 OA 完全一致） =====
            Map<Long, V3xOrgAdapter.Member> memberMap = new LinkedHashMap<>();
            String startMemberId = (String) param.get("startMemberId");
            if (Strings.isNotBlank(toIds)) {
                Set<V3xOrgAdapter.Member> toSet = V3xOrgAdapter.getMembersByTypeAndIds(toIds);
                for (V3xOrgAdapter.Member m : toSet) {
                    if (!memberMap.containsKey(m.getId())) {
                        if (m.getId().equals(user.getId())) {
                            if (Strings.isNotBlank(startMemberId)) {
                                if (!m.getId().equals(Long.parseLong(startMemberId))) continue;
                            }
                        }
                        memberMap.put(m.getId(), m);
                    }
                }
            }
            if (Strings.isNotBlank(ccIds)) {
                Set<V3xOrgAdapter.Member> ccSet = V3xOrgAdapter.getMembersByTypeAndIds(ccIds);
                for (V3xOrgAdapter.Member m : ccSet) {
                    if (!memberMap.containsKey(m.getId())) memberMap.put(m.getId(), m);
                }
            }

            // ===== 涉密判断 =====
            boolean isSecret = false;
            if (Strings.isNotBlank(fslId)) {
                Object fslObj = new FileSecretManager().getById(Long.parseLong(fslId));
            FileSecretLevel fsl = (fslObj instanceof FileSecretLevel) ? (FileSecretLevel) fslObj : null;
                isSecret = (fsl.getIsFileSecret() != null && fsl.getIsFileSecret() == 1);
            }

            // ===== 组装事项列表 =====
            List<InMailAffair> affairList = new ArrayList<>();

            // 发件人自己的事务
            InMailAffair sentAffair = new InMailAffair();
            sentAffair.setSize(size);
            InMailUtil.makeInMailAffair(sentAffair, bean, user, subject,
                    InMailConstant.InMailAffairState.sent.ordinal(), user.getId());
            sentAffair.setPassTheAudit(isSecret ? 0 : 1);
            affairList.add(sentAffair);

            // 每个收件人一条事务
            for (V3xOrgAdapter.Member member : memberMap.values()) {
                InMailAffair recAffair = new InMailAffair();
                recAffair.setSize(size);
                InMailUtil.makeInMailAffair(recAffair, bean, user, subject,
                        InMailConstant.InMailAffairState.run.ordinal(), member.getId());
                recAffair.setPassTheAudit(isSecret ? 0 : 1);
                affairList.add(recAffair);
            }

            // ===== 保存 =====
            mmgr.saveInMailSummary(bean, affairList, isExist);

            resultMsg.put("code", "10001");
            resultMsg.put("summaryId", bean.getId());
            if (!affairList.isEmpty()) resultMsg.put("affairId", affairList.get(0).getId());

        } catch (Exception e) {
            log.error("邮件发送失败", e);
            resultMsg.put("code", "10004");
            resultMsg.put("msg", "邮件发送失败,失败原因：系统内部错误" + e.getMessage());
        }
        return resultMsg;
    }

    // ==================== save 草稿 — 从 OA NewEmailUtils.save 完整迁移 ====================

    public static Map<String, Object> save(Map<String, Object> param, InternalMailManager mmgr) {
        Map<String, Object> resultMsg = new LinkedHashMap<>();
        resultMsg.put("code", "10001");
        try {
            UserInfo user = AppContext.getCurrentUser();
            String text = Objects.toString(param.get("content"), "");
            boolean isCross = Boolean.TRUE.equals(param.get("kuaWang"));
            String summaryId = (String) param.get("summaryId");
            String affairId = (String) param.get("affairId");
            String attachments = (String) param.get("attachments");
            String to = Objects.toString(param.get("receiversStr"), "");
            String toIds = Objects.toString(param.get("receivers"), "");
            String cc = Objects.toString(param.get("copyReceiversStr"), "");
            String ccIds = Objects.toString(param.get("copyReceivers"), "");
            String approver = (String) param.get("approver");
            String approverStr = (String) param.get("approverStr");
            String subject = Objects.toString(param.get("subject"), "");
            String fslId = (String) param.get("secretTypeId");
            boolean timedTask = Boolean.TRUE.equals(param.get("timedTask"));
            String timingDateStr = (String) param.get("timingDate");
            String editMailSend = (String) param.get("editMailSend");
            String autosave = param.containsKey("autosave") ?
                    (param.get("autosave") != null ? param.get("autosave").toString() : null) : null;

            Timestamp firstAutosaveTime = null;
            if (param.containsKey("firstAutosaveTime") && param.get("firstAutosaveTime") != null)
                firstAutosaveTime = new Timestamp(Long.parseLong(param.get("firstAutosaveTime").toString()));

            // 草稿重保存：删除旧草稿
            if ("1".equals(editMailSend) && affairId != null) {
                InMailAffair old = mmgr.getInMailAffairById(Long.parseLong(affairId));
                if (old != null && old.getState() == 1) {
                    mmgr.updateFieldById("IS_DELETE", 1, null, Long.parseLong(affairId));
                }
            }

            // UTF8 空格修正
            String shortMsg = (String) param.get("shortMessage");
            try {
                byte[] space = new byte[]{(byte) 0xc2, (byte) 0xa0};
                String UTFSpace = new String(space, "UTF-8");
                subject = subject.replaceAll(UTFSpace, " ");
            } catch (Exception ignored) {}

            if (text == null) text = "";
            if (subject == null) subject = "";
            if (shortMsg == null) shortMsg = "";

            InMailSummary bean = null;
            if (Strings.isBlank(editMailSend)) {
                if (Strings.isNotBlank(summaryId)) bean = mmgr.getInMailSummaryById(Long.parseLong(summaryId));
                if (Strings.isNotBlank(affairId) && bean == null) {
                    InMailAffair aff = mmgr.getInMailAffairById(Long.parseLong(affairId));
                    if (aff != null) bean = mmgr.getInMailSummaryById(aff.getObjectId());
                }
            }

            boolean isExist = (bean != null);
            if (bean == null) bean = new InMailSummary();

            String mark = (String) param.get("mark");
            if (Strings.isNotBlank(mark)) {
                bean.setMark(mark);
                if ("forwordMail".equals(mark)) {
                    String pid = (String) param.get("parentformSummaryid");
                    if (Strings.isNotBlank(pid)) bean.setParentformSummaryid(Long.parseLong(pid));
                    String paid = (String) param.get("parentformAffairid");
                    if (Strings.isNotBlank(paid)) bean.setParentformAffairid(Long.parseLong(paid));
                    String fid = (String) param.get("forwardMemberId");
                    if (Strings.isNotBlank(fid)) bean.setForwardMemberId(Long.parseLong(fid));
                    String fm = (String) param.get("forwardMember");
                    if (Strings.isNotBlank(fm)) bean.setForwardMember(fm);
                } else if ("reply".equals(mark) || "allReply".equals(mark)) {
                    String rpid = (String) param.get("replyParentSummaryid");
                    if (Strings.isNotBlank(rpid)) bean.setReplyParentSummaryid(Long.parseLong(rpid));
                    String rpaid = (String) param.get("replyParentAffairid");
                    if (Strings.isNotBlank(rpaid)) bean.setReplyParentAffairid(Long.parseLong(rpaid));
                    String rmid = (String) param.get("replyMemberId");
                    if (Strings.isNotBlank(rmid)) bean.setReplyMemberId(Long.parseLong(rmid));
                    String rm = (String) param.get("replyMember");
                    if (Strings.isNotBlank(rm)) bean.setReplyMember(rm);
                }
            }

            bean.setApproverStr(approverStr);
            bean.setApprover(approver);
            bean.setAutosave(autosave);
            bean.setFirstAutosaveTime(firstAutosaveTime);

            InMailUtil.makeInMailSummary(bean, user, subject, to, toIds, cc, ccIds, text, shortMsg);

            // 密级
            if (fslId != null) bean.setFileSecretLevelId(Long.parseLong(fslId));
            else bean.setFileSecretLevelId(1L);

            // 附件
            Long size = 0L;
            if (Strings.isNotBlank(attachments)) bean.setAttachmentsFlag(true);
            else bean.setAttachmentsFlag(false);
            if (Strings.isNotBlank(text)) {
                String tmpPath = System.getProperty("java.io.tmpdir") + File.separator + "draft_" + bean.getId() + ".html";
                writerToFile(tmpPath, text.getBytes("UTF-8"));
                File f = new File(tmpPath);
                if (f.exists()) size += f.length();
                delteFile(tmpPath);
            }
            bean.setSize(size);

            bean.setTimedTask(timedTask);
            if (bean.getTimedTask() && Strings.isNotBlank(timingDateStr)) {
                bean.setTimingDate(Timestamp.valueOf(timingDateStr));
            }
            bean.setKuaWang(isCross);

            List<InMailAffair> list = new ArrayList<>();
            InMailAffair draftAffair = new InMailAffair();
            draftAffair.setSize(size);
            InMailUtil.makeInMailAffair(draftAffair, bean, user, subject,
                    InMailConstant.InMailAffairState.draf.ordinal(), user.getId());
            list.add(draftAffair);

            mmgr.saveInMailSummary(bean, list, isExist);

            resultMsg.put("code", "10001");
            resultMsg.put("summaryId", bean.getId());
            resultMsg.put("affairId", draftAffair.getId());
            resultMsg.put("msg", bean.getTimedTask() ? "定时成功" : "存为草稿成功");
        } catch (Exception e) {
            log.error("草稿保存失败", e);
            resultMsg.put("code", "10004");
            resultMsg.put("msg", "草稿保存失败：" + e.getMessage());
        }
        return resultMsg;
    }

    // ==================== 编辑/回复/转发 数据提取 ====================

    public static Map<String, Object> internalCompile(Map<String, Object> param, InternalMailManager mmgr) {
        String summaryId = (String) param.get("summaryId");
        String affairId = (String) param.get("affairId");
        String mark = (String) param.get("mark");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("mark", "new");

        if ("reEdit".equals(mark) && Strings.isNotBlank(summaryId)) {
            InMailSummary s = mmgr.getInMailSummaryById(Long.parseLong(summaryId));
            if (s != null) {
                formatMember(s); // 去掉undefined
                try { s.setContent(EmailDESUtil.getDecryptString(s.getContent())); } catch (Exception ignored) {}
                data.put("mark", "reEdit");
                data.put("subject", s.getSubject());
                data.put("sendTo", s.getSendTo());
                data.put("sendToIds", s.getSendToIds());
                data.put("copyTo", s.getCopyTo());
                data.put("copyToIds", s.getCopyToIds());
                data.put("content", s.getContent());
                data.put("summaryId", summaryId);
            }
        }
        return data;
    }

    public static Map<String, Object> replyData(Map<String, Object> param) {
        String summaryId = (String) param.get("summaryId");
        String affairId = (String) param.get("affairId");
        Map<String, Object> data = new LinkedHashMap<>();

        if (Strings.isNotBlank(summaryId)) {
            InMailSummary s = DBAgent.get(InMailSummary.class, Long.parseLong(summaryId));
            if (s != null) {
                formatMember(s);
                String content = s.getContent();
                try { content = EmailDESUtil.getDecryptString(content); } catch (Exception ignored) {}
                if (Strings.isBlank(content)) content = "&nbsp;";

                V3xOrgAdapter.Member member = V3xOrgAdapter.toMember(s.getStartMemberId());
                data.put("subject", s.getSubject());
                data.put("sendTo", member != null ? member.getName() : "");
                data.put("sendToIds", "Member|" + s.getStartMemberId());
                data.put("content", InMailUtil.mailSignature() + "<br/><br/>" + toContent(member, s, content));
                data.put("mark", "reply");
                data.put("summaryId", summaryId);
                data.put("affairId", affairId);
            }
        }
        return data;
    }

    // ==================== Helpers（从 OA 原样复制） ====================

    /** UTF8 空格修正后的主题 */
    private static String fixSpaces(String s) {
        if (s == null) return "";
        try {
            byte[] sp = new byte[]{(byte) 0xc2, (byte) 0xa0};
            return s.replaceAll(new String(sp, "UTF-8"), " ");
        } catch (Exception e) { return s; }
    }

    /** 去掉 sendTo/sendToIds/copyTo/copyToIds 中的 "undefined" （OA formatMember 原样逻辑） */
    private static void formatMember(InMailSummary bean) {
        if (bean == null) return;
        String sendToIds = bean.getSendToIds(), sendTo = bean.getSendTo();
        List<String> idList = new ArrayList<>(), nameList = new ArrayList<>();

        if (sendToIds != null) for (String s : sendToIds.split(","))
            if (StringUtils.isNotBlank(s) && !s.contains("undefined")) idList.add(s);
        bean.setSendToIds(idList.isEmpty() ? null : StringUtils.join(idList, ","));

        if (sendTo != null) for (String s : sendTo.split(","))
            if (StringUtils.isNotBlank(s) && !s.contains("undefined")) nameList.add(s);
        bean.setSendTo(nameList.isEmpty() ? null : StringUtils.join(nameList, ","));

        String copyTo = bean.getCopyTo(), copyToIds = bean.getCopyToIds();
        nameList.clear(); idList.clear();
        if (copyToIds != null) for (String s : copyToIds.split(","))
            if (StringUtils.isNotBlank(s) && !s.contains("undefined")) idList.add(s);
        bean.setCopyToIds(idList.isEmpty() ? null : StringUtils.join(idList, ","));
        if (copyTo != null) for (String s : copyTo.split(","))
            if (StringUtils.isNotBlank(s) && !s.contains("undefined")) nameList.add(s);
        bean.setCopyTo(nameList.isEmpty() ? null : StringUtils.join(nameList, ","));
    }

    /** 构建回复/转发引用文本（OA toContent 原样逻辑） */
    private static String toContent(V3xOrgAdapter.Member member, InMailSummary bean, String content) {
        StringBuilder rs = new StringBuilder();
        try {
            rs.append("<p>--------------------------------原始文件---------------------------</p>");
            rs.append("<p>主题：").append(bean.getSubject()).append("</p>");

            String startDetail = bean.getStartDetail();
            if (startDetail != null) {
                String[] parts = startDetail.split("-");
                if (parts.length > 5)
                    rs.append("<p>发件人：").append(parts[1]).append("( ").append(parts[3]).append(" - ").append(parts[4]).append(" )</p>");
            } else if (member != null) {
                rs.append("<p>发件人：").append(member.getName()).append("</p>");
            }

            rs.append("<p>收件人：").append(bean.getSendTo() != null ? bean.getSendTo() : "").append("</p>");
            rs.append("<p>抄送人：").append(bean.getCopyTo() != null ? bean.getCopyTo() : "").append("</p>");
            rs.append("<p>&nbsp;</p>");
            rs.append("<p>").append(content).append("</p>");
        } catch (Exception e) {
            log.error("toContent error", e);
        }
        return rs.toString();
    }

    /** 写文件 */
    private static void writerToFile(String path, byte[] data) {
        RandomAccessFile writer = null;
        try {
            File f = new File(path);
            if (f.exists() && f.isFile()) f.delete();
            f.createNewFile();
            writer = new RandomAccessFile(f, "rw");
            writer.write(data);
        } catch (Exception ignored) {
        } finally {
            try { if (writer != null) writer.close(); } catch (Exception ignored) {}
        }
    }

    /** 删文件 */
    private static void delteFile(String path) {
        File f = new File(path);
        if (f.exists() && f.isFile()) f.delete();
    }
}
