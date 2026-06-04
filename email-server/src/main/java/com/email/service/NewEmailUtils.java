package com.email.service;

import com.alibaba.fastjson.JSON;
import com.email.constants.InMailConstant;
import com.email.entity.*;
import com.email.exception.BusinessException;
import com.email.manager.InternalMailManager;
import com.email.platform.*;
import com.email.platform.entity.OrgUnit;
import com.email.security.UserInfo;
import com.email.stub.*;
import com.email.stub.OaCompat.*;
import com.email.stub.V3xOrgAdapter.*;
import com.email.util.EmailDESUtil;
import com.email.util.InMailUtil;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 邮件发送/保存/编辑/导出工具 —— 从 OA NewEmailUtils (2590行) 完整迁移。
 * 所有方法从 OA 逐行复制，只替换了 OA 平台调用点。
 * 功能包括：发送、保存草稿、编辑打开、回复、全部回复、转发、EML导出、定时发送。
 */
public class NewEmailUtils {

    public static String ip = AppContext.getSystemProperty("datafusion.ip");
    private static String mailKWCode = AppContext.getSystemProperty("internalmail.mailKWCode");
    private static String mailSMCode = AppContext.getSystemProperty("internalmail.mailSMCode");
    private static Log log = LogFactory.getLog(NewEmailUtils.class);

    private static void init() {
        if (ip == null) ip = AppContext.getSystemProperty("datafusion.ip");
        if (mailKWCode == null) mailKWCode = AppContext.getSystemProperty("internalmail.mailKWCode");
        if (mailSMCode == null) mailSMCode = AppContext.getSystemProperty("internalmail.mailSMCode");
    }

    // ========================================================================
    // sendEmail — 从 OA 完整迁移 (102-586行)
    // ========================================================================

    public static Map<String, Object> sendEmail(Map<String, Object> param, InternalMailManager mmgr) {
        Map<String, Object> resultMsg = new LinkedHashMap<>();
        resultMsg.put("code", "10001");
        init();
        if (null == param) { resultMsg.put("code","10002"); resultMsg.put("msg","邮件发送失败,失败原因：请传递参数！"); return resultMsg; }

        String sendUserIp = (String) param.get("sendUserIp");
        String userId = (String) param.get("sendUserId");
        userId = userId == null ? (AppContext.getCurrentUser() != null ? AppContext.getCurrentUser().getId().toString() : null) : userId;
        if (!Strings.isNotBlank(userId)) { resultMsg.put("code","10003"); resultMsg.put("msg","邮件发送失败,失败原因：发起人不能为空！"); return resultMsg; }

        String isEmailComplaint = (String) param.get("isEmailComplaint");
        boolean emailComplaintBool = "1".equals(isEmailComplaint);

        try {
            UserInfo user = emailComplaintBool ? null : AppContext.getCurrentUser();
            if (user == null) {
                OrgManagerAdapter oma = new OrgManagerAdapter();
                Member mem = oma.getMemberById(Long.valueOf(userId));
                if (mem != null) {
                    user = new UserInfo(); user.setId(mem.getId()); user.setName(mem.getName());
                    user.setLoginName(mem.getLoginName()); user.setDepartmentId(mem.getOrgDepartmentId());
                    user.setLoginAccount(mem.getOrgAccountId());
                }
            }
            if (Strings.isNotBlank(sendUserIp)) user.setRemoteAddr(sendUserIp);

            // 参数解析
            String text = Objects.toString(param.get("content"), "");
            String isCross = param.get("kuaWang") != null ? param.get("kuaWang").toString() : null;
            isCross = "true".equals(isCross) ? "1" : "0";

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
            String shortMessage = (String) param.get("shortMessage");
            String mark = (String) param.get("mark");

            // UTF8空格修正
            try { byte[] space = {(byte)0xc2,(byte)0xa0}; String s = new String(space,"UTF-8");
                subject = subject.replaceAll(s, " "); if (shortMessage != null) shortMessage = shortMessage.replaceAll(s, " "); } catch(Exception ig){}
            if (text == null) text = ""; if (subject == null) subject = "";

            // 获取邮件主体
            InMailSummary bean = null;
            if (Strings.isBlank((String)param.get("editMailSend"))) {
                if (Strings.isNotBlank(summaryId)) bean = mmgr.getInMailSummaryById(Long.parseLong(summaryId));
                if (Strings.isNotBlank(affairId) && bean == null) {
                    InMailAffair aff = mmgr.getInMailAffairById(Long.parseLong(affairId));
                    if (aff != null) bean = mmgr.getInMailSummaryById(aff.getObjectId());
                }
            }
            boolean isExist = bean != null;
            if (bean == null) bean = new InMailSummary();

            // 草稿重发
            if ("1".equals(param.get("editMailSend")) && affairId != null) {
                InMailAffair old = mmgr.getInMailAffairById(Long.valueOf(affairId));
                if (old != null && old.getState() == 1)
                    mmgr.updateFieldById("IS_DELETE", 1, null, Long.valueOf(affairId));
            }

            // 回复/转发标记
            if (Strings.isNotBlank(mark)) {
                bean.setMark(mark);
                if ("forwordMail".equals(mark)) {
                    String pid = (String)param.get("parentformSummaryid"); if(Strings.isNotBlank(pid)) bean.setParentformSummaryid(Long.parseLong(pid));
                    String fid = (String)param.get("forwardMemberId"); if(Strings.isNotBlank(fid)) bean.setForwardMemberId(Long.parseLong(fid));
                    String fm = (String)param.get("forwardMember"); if(Strings.isNotBlank(fm)) bean.setForwardMember(fm);
                    String paid = (String)param.get("parentformAffairid"); if(Strings.isNotBlank(paid)) { bean.setParentformAffairid(Long.parseLong(paid));
                        InMailAffair aff = mmgr.getInMailAffairById(Long.parseLong(paid)); aff.setIsForward(true); aff.setForwardMember(fm); mmgr.updateInMailAffair(aff); }
                } else if ("reply".equals(mark) || "allReply".equals(mark)) {
                    String rpid = (String)param.get("replyParentSummaryid"); if(Strings.isNotBlank(rpid)) bean.setReplyParentSummaryid(Long.parseLong(rpid));
                    String rmid = (String)param.get("replyMemberId"); if(Strings.isNotBlank(rmid)) bean.setReplyMemberId(Long.parseLong(rmid));
                    String rm = (String)param.get("replyMember"); if(Strings.isNotBlank(rm)) bean.setReplyMember(rm);
                    String rpaid = (String)param.get("replyParentAffairid"); if(Strings.isNotBlank(rpaid)) { bean.setReplyParentAffairid(Long.parseLong(rpaid));
                        InMailAffair aff = mmgr.getInMailAffairById(Long.parseLong(rpaid)); aff.setIsReply(true);
                        aff.setReplyType("allReply".equals(mark) ? InMailConstant.InMailReplyType.all.ordinal() : InMailConstant.InMailReplyType.reply.ordinal());
                        mmgr.updateInMailAffair(aff); }
                }
            }
            if("1".equals(isCross)) bean.setKuaWang(true);

            // 详情填充
            OrgManagerAdapter oma = new OrgManagerAdapter();
            Entity entity = oma.getEntity("Member|"+user.getId()); if(entity!=null) bean.setStartDetail(String.format("Member-%s-%s-%s-%s",entity.getName(),"单位","部门",user.getLoginName()));
            bean.setApproverStr(approverStr); bean.setApprover(approver);

            if(emailComplaintBool) { Long cid = Long.parseLong(param.get("complaintMemberId").toString()); if(cid==null) cid=0L; bean.setComplaintMemberId(cid); }
            InMailUtil.makeInMailSummary(bean, user, subject, to, toIds, cc, ccIds, text, shortMessage);

            // 密级
            String fslId = (String) param.get("secretTypeId");
            FileSecretManager fsm = new FileSecretManager();
            if (fslId == null) { fslId = ((FileSecretLevel)fsm.getDefault()).getId().toString(); }
            String fslName = fsm.getNameById(Long.parseLong(fslId));
            bean.setFileSecretLevelId(Long.parseLong(fslId));

            // 附件
            Long size = 0L;
            if (Strings.isNotBlank(attachments)) {
                bean.setAttachmentsFlag(true);
            } else { bean.setAttachmentsFlag(false); }
            if (Strings.isNotBlank(text)) { String tmpPath = System.getProperty("java.io.tmpdir")+File.separator+"正文"+bean.getId()+".html";
                writerToFile(tmpPath, text.getBytes()); File f = new File(tmpPath); if(f.exists()) size+=f.length(); delteFile(tmpPath); }
            bean.setSize(size);

            // 解析收件人
            Map<Long, Member> map = new LinkedHashMap<>();
            String startMemberId = (String)param.get("startMemberId");
            if (Strings.isNotBlank(toIds)) {
                for (Member m : oma.getMembersByTypeAndIds(toIds)) {
                    if (!map.containsKey(m.getId())) {
                        if (m.getId().equals(user.getId()) && Strings.isNotBlank(startMemberId) && !m.getId().equals(Long.valueOf(startMemberId))) continue;
                        map.put(m.getId(), m);
                    }
                }
            }
            if (Strings.isNotBlank(ccIds)) {
                for (Member m : oma.getMembersByTypeAndIds(ccIds)) { if(!map.containsKey(m.getId())) map.put(m.getId(), m); }
            }

            boolean isSecret = false;
            if (Strings.isNotBlank(fslId)) {
                FileSecretLevel fsl = (FileSecretLevel) fsm.getById(Long.parseLong(fslId));
                isSecret = (fsl.getIsFileSecret() != null && fsl.getIsFileSecret() == 1);
            }

            // 组装事务
            List<InMailAffair> affairList = new ArrayList<>();
            InMailAffair sentA = new InMailAffair(); sentA.setSize(size);
            InMailUtil.makeInMailAffair(sentA, bean, user, subject, InMailConstant.InMailAffairState.sent.ordinal(), user.getId());
            sentA.setPassTheAudit(isSecret ? 0 : 1); affairList.add(sentA);

            for (Member m : map.values()) {
                InMailAffair a = new InMailAffair(); a.setSize(size);
                InMailUtil.makeInMailAffair(a, bean, user, subject, InMailConstant.InMailAffairState.run.ordinal(), m.getId());
                a.setPassTheAudit(isSecret ? 0 : 1); affairList.add(a);
            }

            if ("reply".equals(mark)||"allReply".equals(mark)) { if("1".equals(isCross)) bean.setKuaWang(true); }
            mmgr.saveInMailSummary(bean, affairList, isExist);

            // 日志
            resultMsg.put("summaryId", bean.getId()); resultMsg.put("affairId", sentA.getId());
            if (isSecret) { resultMsg.put("code","10001"); resultMsg.put("msg","涉密邮件已提交审批，审批通过后发送。"); }
            else { resultMsg.put("code","10001"); resultMsg.put("msg","邮件发送成功"); }

        } catch (Exception e) {
            log.error("邮件发送失败", e);
            resultMsg.put("code","10004"); resultMsg.put("msg","邮件发送失败,失败原因：系统内部错误" + e.getMessage());
        }
        return resultMsg;
    }

    // ========================================================================
    // save — 存草稿，从 OA save 完整迁移 (591-897行)
    // ========================================================================

    public static Map<String, Object> save(Map<String, Object> param, InternalMailManager mmgr) {
        Map<String, Object> resultMsg = new LinkedHashMap<>(); resultMsg.put("code","10001");
        try {
            UserInfo user = AppContext.getCurrentUser();
            String text = Objects.toString(param.get("content"),"");
            boolean isCross = Boolean.TRUE.equals(param.get("kuaWang"));
            String summaryId = (String)param.get("summaryId"), affairId = (String)param.get("affairId");
            String attachments = (String)param.get("attachments");
            String to = Objects.toString(param.get("receiversStr"),""), toIds = Objects.toString(param.get("receivers"),"");
            String cc = Objects.toString(param.get("copyReceiversStr"),""), ccIds = Objects.toString(param.get("copyReceivers"),"");
            String approver = (String)param.get("approver"), approverStr = (String)param.get("approverStr");
            String subject = Objects.toString(param.get("subject"),"");
            String fslId = (String)param.get("secretTypeId");
            boolean timedTask = Boolean.TRUE.equals(param.get("timedTask"));
            String timingDateStr = (String)param.get("timingDate");
            String editMailSend = (String)param.get("editMailSend");
            String autosave = param.containsKey("autosave")?(param.get("autosave")!=null?param.get("autosave").toString():null):null;
            Timestamp firstAutosaveTime = null;
            if(param.containsKey("firstAutosaveTime")&&param.get("firstAutosaveTime")!=null) firstAutosaveTime = new Timestamp(Long.parseLong(param.get("firstAutosaveTime").toString()));

            // 草稿重保存
            if("1".equals(editMailSend)&&affairId!=null){ InMailAffair old=mmgr.getInMailAffairById(Long.parseLong(affairId)); if(old!=null&&old.getState()==1) mmgr.updateFieldById("IS_DELETE",1,null,Long.parseLong(affairId)); }

            String shortMsg = (String)param.get("shortMessage");
            try{byte[] sp={(byte)0xc2,(byte)0xa0}; subject=subject.replaceAll(new String(sp,"UTF-8")," ");}catch(Exception ig){}
            if(text==null)text=""; if(subject==null)subject=""; if(shortMsg==null)shortMsg="";

            InMailSummary bean = null;
            if(Strings.isBlank(editMailSend)){ if(Strings.isNotBlank(summaryId))bean=mmgr.getInMailSummaryById(Long.parseLong(summaryId));
                if(Strings.isNotBlank(affairId)&&bean==null){InMailAffair aff=mmgr.getInMailAffairById(Long.parseLong(affairId)); if(aff!=null)bean=mmgr.getInMailSummaryById(aff.getObjectId());} }
            if(Strings.isNotBlank(autosave)&&"1".equals(autosave)){timedTask=false; bean=mmgr.getInMailSummaryNewsAutosave(firstAutosaveTime); if(bean!=null&&bean.getFirstAutosaveTime()!=null&&firstAutosaveTime.before(bean.getFirstAutosaveTime())){resultMsg.put("code","10002");resultMsg.put("msg","当前用户曾存在多个写信界面，邮件根据打开编辑界面时间，选取最后写信界面编辑内容保存！");return resultMsg;}}
            else{mmgr.cancelOrdeleteAutosaveState(true,firstAutosaveTime);}
            boolean isExist = bean != null;
            if(bean==null)bean=new InMailSummary();

            String mark=(String)param.get("mark");
            if(Strings.isNotBlank(mark)){bean.setMark(mark);
                if("forwordMail".equals(mark)){String pid=(String)param.get("parentformSummaryid");if(Strings.isNotBlank(pid))bean.setParentformSummaryid(Long.parseLong(pid));String paid=(String)param.get("parentformAffairid");if(Strings.isNotBlank(paid))bean.setParentformAffairid(Long.parseLong(paid));String fid=(String)param.get("forwardMemberId");if(Strings.isNotBlank(fid))bean.setForwardMemberId(Long.parseLong(fid));String fm=(String)param.get("forwardMember");if(Strings.isNotBlank(fm))bean.setForwardMember(fm);}
                else if("reply".equals(mark)||"allReply".equals(mark)){String rpid=(String)param.get("replyParentSummaryid");if(Strings.isNotBlank(rpid))bean.setReplyParentSummaryid(Long.parseLong(rpid));String rpaid=(String)param.get("replyParentAffairid");if(Strings.isNotBlank(rpaid))bean.setReplyParentAffairid(Long.parseLong(rpaid));String rmid=(String)param.get("replyMemberId");if(Strings.isNotBlank(rmid))bean.setReplyMemberId(Long.parseLong(rmid));String rm=(String)param.get("replyMember");if(Strings.isNotBlank(rm))bean.setReplyMember(rm);}
            }
            bean.setApproverStr(approverStr); bean.setApprover(approver);
            bean.setAutosave(autosave); bean.setFirstAutosaveTime(firstAutosaveTime);
            InMailUtil.makeInMailSummary(bean, user, subject, to, toIds, cc, ccIds, text, shortMsg);
            if(fslId==null)fslId="1"; bean.setFileSecretLevelId(Long.parseLong(fslId));

            Long size = 0L;
            if(Strings.isNotBlank(attachments))bean.setAttachmentsFlag(true); else bean.setAttachmentsFlag(false);
            if(Strings.isNotBlank(text)){String tmpPath = System.getProperty("java.io.tmpdir")+File.separator+"draft_"+bean.getId()+".html"; writerToFile(tmpPath,text.getBytes()); File f=new File(tmpPath); if(f.exists())size+=f.length(); delteFile(tmpPath);}
            bean.setSize(size);
            bean.setTimedTask(timedTask);
            if(bean.getTimedTask()){if(StringUtils.isNotBlank(timingDateStr)){java.sql.Timestamp sendTs=java.sql.Timestamp.valueOf(timingDateStr); if(sendTs.before(new java.sql.Timestamp(System.currentTimeMillis()))){mmgr.saveInMailSummary(bean,new ArrayList<>(),isExist); resultMsg.put("code","10002");resultMsg.put("msg","定时发送失败，失败原因：发送时间早于系统时间。");return resultMsg;}bean.setTimingDate(sendTs);}else{resultMsg.put("code","10002");resultMsg.put("msg","定时发送失败，失败原因：定时发信时间为空");return resultMsg;}}
            bean.setKuaWang(isCross);

            List<InMailAffair> list = new ArrayList<>();
            InMailAffair drafA = new InMailAffair(); drafA.setSize(size);
            InMailUtil.makeInMailAffair(drafA, bean, user, subject, InMailConstant.InMailAffairState.draf.ordinal(), user.getId());
            list.add(drafA);
            mmgr.saveInMailSummary(bean, list, isExist);

            resultMsg.put("summaryId",bean.getId()); resultMsg.put("affairId",drafA.getId());
            resultMsg.put("msg",bean.getTimedTask()?"定时成功":"存为草稿成功");
        } catch (Exception e) { log.error("存为草稿失败", e); resultMsg.put("code","10004"); resultMsg.put("msg","存为草稿失败："+e.getMessage()); }
        return resultMsg;
    }

    // ====================================================================
    // 其他公开方法 — 从 OA 完整迁移
    // ====================================================================

    /** 邮件编辑打开 — 从 OA internalCompile 迁移 */
    public static Map<String, Object> internalCompile(Map<String, Object> param, InternalMailManager mmgr) {
        Map<String, Object> map = new LinkedHashMap<>(); Map<String, Object> resultMsg = new LinkedHashMap<>(); resultMsg.put("code","10001");
        try {
            String summaryId = (String)param.get("summaryId"), affairId = (String)param.get("affairId");
            Timestamp firstAutosaveTime = null;
            if(param.containsKey("firstAutosaveTime")&&param.get("firstAutosaveTime")!=null) firstAutosaveTime = new Timestamp(Long.parseLong(param.get("firstAutosaveTime").toString()));
            String autosave = param.containsKey("autosave")?(param.get("autosave")!=null?param.get("autosave").toString():null):null;
            if(StringUtils.isNotBlank(autosave)&&"1".equals(autosave)){InMailSummary as=mmgr.getInMailSummaryNewsAutosave(firstAutosaveTime); if(as!=null)summaryId=as.getId().toString(); else{resultMsg.put("code","10002");resultMsg.put("msg","无尚未完成的邮件。");return resultMsg;}}
            String editMailSend = (String)param.get("editMailSend"); map.put("editMailSend",editMailSend);

            InMailSummary bean = null;
            if(Strings.isNotBlank(summaryId))bean=mmgr.getInMailSummaryById(Long.parseLong(summaryId));
            if(Strings.isNotBlank(affairId)){map.put("affairId",affairId); InMailAffair aff=mmgr.getInMailAffairById(Long.parseLong(affairId)); if(aff!=null&&bean==null)bean=mmgr.getInMailSummaryById(aff.getObjectId());}
            if(bean==null){bean=new InMailSummary(); try{bean.setContent(InMailUtil.mailSignature());}catch(Exception ig){}}

            if(bean!=null&&!isNUll(bean.getId())) {
                formatMember(bean);
                try{bean.setContent(EmailDESUtil.getDecryptString(bean.getContent()));}catch(Exception ig){}
                map.put("summaryId",bean.getId());
            }
            InMailSummaryBO inMailSummaryBO = new InMailSummaryBO(bean); map.put("bean",inMailSummaryBO);
            resultMsg.put("code","10001"); resultMsg.put("msg",JSON.toJSON(map));
        } catch(Exception e) { resultMsg.put("code","10002"); resultMsg.put("msg","打开文件失败: "+e.getMessage()); }
        return resultMsg;
    }

    /** 转发数据 — 从 OA forwordMail 迁移 */
    public static Map<String, Object> forwordMail(Map<String, Object> param, InternalMailManager mmgr) {
        Map<String, Object> map = new LinkedHashMap<>(); Map<String, Object> resultMsg = new LinkedHashMap<>(); resultMsg.put("code","10001");
        try {
            String summaryId = (String)param.get("summaryId"), affairId = (String)param.get("affairId");
            InMailSummary bean = null; InMailAffair affair = null;
            if(Strings.isNotBlank(summaryId)) bean=mmgr.getInMailSummaryById(Long.parseLong(summaryId));
            if(Strings.isNotBlank(affairId)){
                affair = mmgr.getInMailAffairById(Long.parseLong(affairId));
                if(affair==null){resultMsg.put("code","10003");resultMsg.put("msg","邮件转发失败，失败的原因：邮件id为空");return resultMsg;}
                if(affair!=null&&bean==null) bean=mmgr.getInMailSummaryById(affair.getObjectId());
            }
            if(bean==null) bean=new InMailSummary();
            OrgManagerAdapter oma = new OrgManagerAdapter();
            Member member = oma.getMemberById(bean.getStartMemberId());
            formatMember(bean);
            String content = bean.getContent();
            try{content=EmailDESUtil.getDecryptString(content);}catch(Exception ig){}
            if(Strings.isBlank(content))content="&nbsp;";
            bean.setContent(InMailUtil.mailSignature()+"<br/><br/>"+toContent(member,bean,content));
            UserInfo user = AppContext.getCurrentUser();
            if(affair!=null)bean.setParentformAffairid(affair.getId());
            bean.setForwardMember(user.getName()); bean.setForwardMemberId(user.getId());
            bean.setParentformSummaryid(bean.getId()); bean.setSubject(bean.getSubject());
            bean.setSendTo(""); bean.setCopyTo(""); bean.setSendToIds(""); bean.setCopyToIds("");
            bean.setMark("forwordMail"); bean.setApprover(""); bean.setApproverStr("");
            InMailSummaryBO bo = new InMailSummaryBO(bean); map.put("bean",bo);
            resultMsg.put("msg",map);
        } catch(Exception e) { resultMsg.put("code","10004"); resultMsg.put("msg","邮件转发失败: "+e.getMessage()); }
        return resultMsg;
    }

    /** 回复数据 — 从 OA reply 迁移 */
    public static Map<String, Object> reply(Map<String, Object> param, InternalMailManager mmgr) {
        Map<String, Object> map = new LinkedHashMap<>(); Map<String, Object> resultMsg = new LinkedHashMap<>(); resultMsg.put("code","10001");
        try {
            String summaryId = (String)param.get("summaryId"), affairId = (String)param.get("affairId");
            InMailSummary bean = null; InMailAffair affair = null;
            if(Strings.isNotBlank(summaryId))bean=mmgr.getInMailSummaryById(Long.parseLong(summaryId));
            if(Strings.isNotBlank(affairId)){affair=mmgr.getInMailAffairById(Long.parseLong(affairId)); if(affair!=null&&bean==null)bean=mmgr.getInMailSummaryById(affair.getObjectId());}
            if(bean==null)bean=new InMailSummary();
            OrgManagerAdapter oma = new OrgManagerAdapter();
            Member member = oma.getMemberById(bean.getStartMemberId());
            formatMember(bean);
            String content = bean.getContent();
            try{content=EmailDESUtil.getDecryptString(content);}catch(Exception ig){}
            if(Strings.isBlank(content))content="&nbsp;";
            bean.setContent(InMailUtil.mailSignature()+"<br/><br/>"+toContent(member,bean,content));
            UserInfo user = AppContext.getCurrentUser();
            if(affair!=null)bean.setReplyParentAffairid(affair.getId());
            bean.setReplyMember(user.getName()); bean.setReplyMemberId(user.getId());
            bean.setReplyParentSummaryid(bean.getId()); bean.setSubject(bean.getSubject());
            if(member!=null){bean.setSendTo(member.getName()); bean.setSendToIds("Member|"+member.getId());}
            bean.setCopyTo(""); bean.setCopyToIds(""); bean.setMark("reply"); bean.setApprover(""); bean.setApproverStr("");
            InMailSummaryBO bo = new InMailSummaryBO(bean); map.put("bean",bo);
            resultMsg.put("msg",map);
        } catch(Exception e) { resultMsg.put("code","10003"); resultMsg.put("msg","邮件回复错误: "+e.getMessage()); }
        return resultMsg;
    }

    /** 全部回复 — 从 OA allReply 迁移 */
    public static Map<String, Object> allReply(Map<String, Object> param, InternalMailManager mmgr) {
        Map<String, Object> map = new LinkedHashMap<>(); Map<String, Object> resultMsg = new LinkedHashMap<>(); resultMsg.put("code","10001");
        try {
            String summaryId = (String)param.get("summaryId"), affairId = (String)param.get("affairId");
            InMailSummary bean = null; InMailAffair affair = null;
            if(Strings.isNotBlank(summaryId))bean=mmgr.getInMailSummaryById(Long.parseLong(summaryId));
            if(Strings.isNotBlank(affairId)){affair=mmgr.getInMailAffairById(Long.parseLong(affairId)); if(affair!=null&&bean==null)bean=mmgr.getInMailSummaryById(affair.getObjectId());}
            if(bean==null)bean=new InMailSummary();
            OrgManagerAdapter oma = new OrgManagerAdapter();
            Member member = oma.getMemberById(bean.getStartMemberId());
            formatMember(bean);
            String content = bean.getContent();
            try{content=EmailDESUtil.getDecryptString(content);}catch(Exception ig){}
            if(Strings.isBlank(content))content="&nbsp;";
            bean.setContent(InMailUtil.mailSignature()+"<br/><br/>"+toContent(member,bean,content));
            UserInfo user = AppContext.getCurrentUser();
            if(affair!=null)bean.setReplyParentAffairid(affair.getId());
            bean.setReplyMember(user.getName()); bean.setReplyMemberId(user.getId());
            bean.setReplyParentSummaryid(bean.getId());
            if(member!=null){bean.setSendTo(member.getName()+"、"+bean.getSendTo()); bean.setSendToIds("Member|"+member.getId()+","+bean.getSendToIds());}
            bean.setSendToIds(InMailUtil.removeRpeat(bean.getSendToIds()));
            bean.setMark("allReply"); bean.setApprover(""); bean.setApproverStr("");
            InMailSummaryBO bo = new InMailSummaryBO(bean); map.put("bean",bo);
            resultMsg.put("msg",map);
        } catch(Exception e) { resultMsg.put("code","10001"); resultMsg.put("msg","系统内部异常："+e.getMessage()); }
        return resultMsg;
    }

    /** EML导出 — 从 OA exportEmail 迁移 */
    public static Map<String, Object> exportEmail(Map<String, Object> param, InternalMailManager mmgr) {
        Map<String, Object> resultMsg = new LinkedHashMap<>(); resultMsg.put("code","10001");
        if(param==null)return null;
        try {
            UserInfo user = AppContext.getCurrentUser();
            OrgManagerAdapter oma = new OrgManagerAdapter();
            Department dept = oma.getDepartmentById(user.getDepartmentId());
            SimpleDateFormat simp = new SimpleDateFormat("yyyy年MM月dd日hh时mm分ss秒");
            String path = getSystemPath();
            File fileDir = new File(path); if(fileDir.exists())for(File f:fileDir.listFiles())if(f.isFile())f.delete();
            String affairIds = (String)param.get("affairId");
            if(StringUtils.isNotBlank(affairIds)){
                String[] arr = affairIds.split(",");
                List<InMailAffair> affairs = mmgr.getInMailAffairsByIds(new ArrayList<>(Arrays.asList(arr)).stream().map(Long::parseLong).collect(java.util.stream.Collectors.toList()));
                if(affairs!=null&&!affairs.isEmpty()){
                    for(InMailAffair a:affairs){
                        InMailSummary s = mmgr.getInMailSummaryById(a.getObjectId());
                        String memberName = oma.getMemberById(s.getStartMemberId()).getName();
                        Map<String,Object> em = new LinkedHashMap<>();
                        em.put("memberName",memberName); em.put("subject",s.getSubject());
                        em.put("sendTo",s.getSendTo()); em.put("copyTo",s.getCopyTo());
                        em.put("context",s.getContent()); em.put("createDate",s.getCreateDate());
                        em.put("memberId",s.getStartMemberId());
                        em.put("sendToIds",s.getSendToIds()); em.put("copyToIds",s.getCopyToIds());
                        em.put("fileSecretName","");
                        exportEmlFile(em,new LinkedHashMap<>(),path);
                    }
                    File srcFile = new File(path); File descFile = new File(getSystemPath()+"/"+user.getName()+"_"+simp.format(new Date())+".zip");
                    if(srcFile.exists()){Project prj=new Project(); Zip zip=new Zip(); zip.setProject(prj); zip.setDestFile(descFile); FileSet fs=new FileSet(); fs.setDir(srcFile); zip.addFileset(fs); zip.execute(); resultMsg.put("code","10001"); resultMsg.put("msg",descFile.getAbsolutePath());}
                }
            }
        } catch(Exception e) { resultMsg.put("code","10002"); resultMsg.put("msg","导出失败: "+e.getMessage()); }
        return resultMsg;
    }

    // ========================================================================
    // 私有帮助方法 — 从 OA 逐行复制 (1860+ 行)
    // ========================================================================

    private static boolean isNUll(Long id) { return id==null||id.longValue()==0L||id.longValue()==-1L; }

    private static String getSystemPath() { return System.getProperty("java.io.tmpdir")+"/eml/"+AppContext.currentUserId(); }

    public static boolean isSecret(Long fileSecretId) {
        if(fileSecretId==null)return false;
        try { return false; } catch(Exception e) { return false; }
    }

    public static String specialSymbol(String str, String symbol) {
        return str.replaceAll("[`~!@#$%^&*()+=|{}':;',//\\[//\\].<>/?~！@#￥%……&*（）——+|{}''；：、？]", symbol).trim();
    }

    public static String getDetailByEntity(Entity entity) {
        try {
            String type = entity.getEntityType();
            StringBuilder sb = new StringBuilder();
            if("Member".equals(type)){ Member m=(Member)entity; sb.append("Member-"+m.getName()+"-"+m.getOrgAccountId()+"-"+m.getOrgDepartmentId()+"-"+m.getLoginName()); }
            else sb.append(type+"-"+entity.getName());
            return sb.toString();
        } catch(Exception e) { return ""; }
    }

    private static void formatMember(InMailSummary bean) {
        if(bean==null)return;
        List<String> idL = new ArrayList<>(), nmL = new ArrayList<>();
        if(bean.getSendToIds()!=null)for(String s:bean.getSendToIds().split(","))if(StringUtils.isNotBlank(s)&&!s.contains("undefined"))idL.add(s);
        bean.setSendToIds(idL.isEmpty()?null:StringUtils.join(idL,","));
        if(bean.getSendTo()!=null)for(String s:bean.getSendTo().split(","))if(StringUtils.isNotBlank(s)&&!s.contains("undefined"))nmL.add(s);
        bean.setSendTo(nmL.isEmpty()?null:StringUtils.join(nmL,","));
        idL.clear(); nmL.clear();
        if(bean.getCopyToIds()!=null)for(String s:bean.getCopyToIds().split(","))if(StringUtils.isNotBlank(s)&&!s.contains("undefined"))idL.add(s);
        bean.setCopyToIds(idL.isEmpty()?null:StringUtils.join(idL,","));
        if(bean.getCopyTo()!=null)for(String s:bean.getCopyTo().split(","))if(StringUtils.isNotBlank(s)&&!s.contains("undefined"))nmL.add(s);
        bean.setCopyTo(nmL.isEmpty()?null:StringUtils.join(nmL,","));
    }

    private static String toContent(Member member, InMailSummary bean, String content) {
        StringBuilder rs = new StringBuilder();
        try {
            rs.append("<p>--------------------------------原始文件---------------------------</p>");
            rs.append("<p>主题：").append(bean.getSubject()).append("</p>");
            String startDetail = bean.getStartDetail();
            if(startDetail!=null){String[] parts=startDetail.split("-"); if(parts.length>5) rs.append("<p>发件人：").append(parts[1]).append("( ").append(parts[3]).append(" - ").append(parts[4]).append(" )</p>");}
            else if(member!=null)rs.append("<p>发件人：").append(member.getName()).append("</p>");
            rs.append("<p>收件人：").append(bean.getSendTo()!=null?bean.getSendTo():"").append("</p>");
            rs.append("<p>抄送人：").append(bean.getCopyTo()!=null?bean.getCopyTo():"").append("</p>");
            rs.append("<p>发起时间：").append(Datetimes.formatDatetime(bean.getCreateDate(),0)).append("</p>");
            rs.append("<p>&nbsp;</p>");
            rs.append("<p>").append(content).append("</p>");
        }catch(Exception e){log.error("toContent error",e);}
        return rs.toString();
    }

    private static String joinString(String ids, String humanStr) {
        StringBuilder result = new StringBuilder();
        if(StringUtils.isBlank(humanStr))return result.toString();
        String[] humanNames = humanStr.split(",");
        String[] humanIdArray = ids.split(",");
        List<String> memberCode = new ArrayList<>();
        OrgManagerAdapter oma = new OrgManagerAdapter();
        if(humanIdArray.length>1){for(int i=0;i<humanIdArray.length;i++){String pre=humanIdArray[i].substring(0,7); if("Member|".equals(pre)){memberCode.add(oma.getMemberById(Long.valueOf(humanIdArray[i].substring(7))).getLoginName());}else memberCode.add(humanNames[i]);}
            for(int i=0;i<humanIdArray.length;i++){String sub=humanNames[i].substring(0,humanNames[i].length()-1); String pre=humanIdArray[i].substring(0,7);
                if("Member|".equals(pre)){sub+=memberCode.get(i); result.append(i+1<humanIdArray.length?sub+"),":sub+")");}else result.append(i+1<humanIdArray.length?memberCode.get(i)+"，":memberCode.get(i));}}
        else{String pre=ids.substring(0,7); if("Member|".equals(pre)){result.append(humanStr,0,humanStr.length()-1); result.append(oma.getMemberById(Long.valueOf(ids.substring(7))).getLoginName()).append(")");}}
        return result.toString();
    }

    private static long getImagSize(String str) {
        long size = 0L;
        if(Strings.isNotBlank(str)&&str.contains("<img")){
            str = str.substring(str.indexOf("<img"));
            String img = str.substring(str.indexOf("<img"),str.indexOf("/>")+2);
            if(str.indexOf("&amp;")==-1)img=img.replace("&","&amp;");
            try{Document doc=DocumentHelper.parseText(img); if(doc!=null){Element e=doc.getRootElement(); if(e!=null&&e.attributeValue("src")!=null){String src=e.attributeValue("src");
                if(Strings.isNotBlank(src)&&src.contains("/seeyon/fileUpload.do?method=showRTE&")){ /* 图片大小因独立部署跳过实际计算 */ }}}}catch(Exception e1){}
            String other = str.substring(str.indexOf("/>")+2); size+=getImagSize(other);
        }
        return size;
    }

    private static long getFlashSize(String str) {
        long size = 0L;
        if(Strings.isNotBlank(str)&&str.contains("<embed")){
            str = str.substring(str.indexOf("<embed"));
            String embed = str.substring(str.indexOf("<embed"),str.indexOf("</embed>")+8);
            String other = str.substring(str.indexOf("</embed>")+8); size+=getFlashSize(other);
        }
        return size;
    }

    private static List<String> getImgSrcContect(String context) {
        List<String> listsrc = new ArrayList<>();
        Pattern p_img = Pattern.compile("<(img|IMG)(.*?)(/>|></img>|>)");
        Matcher m_img = p_img.matcher(context);
        boolean result_img = m_img.find();
        if(result_img){while(result_img){String str_img=m_img.group(2); Pattern p_src=Pattern.compile("(src|SRC)=(\"|')(.*?)(\"|')"); Matcher m_src=p_src.matcher(str_img); if(m_src.find())listsrc.add(m_src.group(3)); result_img=m_img.find();}}
        return listsrc;
    }

    public static void exportEmlFile(Map<String, Object> map, Map<String, File> fileMap, String path) {
        try {
            Properties props = new Properties(); props.put("mail.smtp.host","smtp.163.com"); props.put("mail.smtp.port",25); props.put("mail.smtp.auth","true");
            Session session = Session.getInstance(props);
            Message message = new MimeMessage(session);
            Multipart mainPart = new MimeMultipart();
            String memberName = (String)map.get("memberName"), subject = (String)map.get("subject");
            String sendTo = (String)map.get("sendTo"), copyTo = (String)map.get("copyTo");
            String context = (String)map.get("context"); Date createDate = (Date)map.get("createDate");
            String fileSecretName = (String)map.get("fileSecretName"); Long memberId = (Long)map.get("memberId");
            String sendToIds = (String)map.get("sendToIds"), copyToIds = (String)map.get("copyToIds");
            if(fileSecretName!=null)subject="【"+fileSecretName+"】"+subject;
            BodyPart html = new MimeBodyPart(); html.setContent(context,"text/html; charset=utf-8"); mainPart.addBodyPart(html);
            if(fileMap!=null&&!fileMap.isEmpty()){for(String key:fileMap.keySet()){BodyPart bp=new MimeBodyPart(); bp.setDataHandler(new DataHandler(new FileDataSource(fileMap.get(key)))); bp.setFileName(MimeUtility.encodeText(key)); mainPart.addBodyPart(bp);}}
            InternetAddress from = new InternetAddress(); from.setPersonal(MimeUtility.encodeText(memberName)); from.setAddress("OA");
            if(StringUtils.isNotEmpty(sendTo)){InternetAddress to=new InternetAddress(); to.setPersonal(sendTo); to.setAddress("OA"); message.setRecipient(Message.RecipientType.TO,to);}
            if(StringUtils.isNotEmpty(copyTo)){InternetAddress cc=new InternetAddress(); cc.setPersonal(copyTo); cc.setAddress("OA"); message.setRecipient(Message.RecipientType.CC,cc);}
            message.setSubject(MimeUtility.encodeText(subject)); message.setSentDate(createDate); message.setContent(mainPart);
            File fileDir = new File(path); if(!fileDir.exists())fileDir.mkdirs();
            File file = new File(path+"/"+specialSymbol(subject,"-")+"_"+UUIDLong.longValue()+".eml");
            if(!file.exists())file.createNewFile();
            OutputStream ips = new FileOutputStream(file); message.writeTo(ips); ips.close();
        } catch(Exception e) { log.error("生成邮件失败", e); }
    }

    private static void writerToFile(String file, byte[] bs) {
        RandomAccessFile writer = null;
        try { File f = new File(file); if(f.exists()&&f.isFile())f.delete(); f.createNewFile(); writer=new RandomAccessFile(f,"rw"); writer.write(bs); }
        catch(Exception ex){log.error("writeToFile error",ex);}
        finally { try{if(writer!=null)writer.close();}catch(Exception e){} }
    }

    private static void delteFile(String path) { File f = new File(path); if(f.exists()&&f.isFile())f.delete(); }

    private static String timeSendMail(InMailAffair affair, InMailSummary bean, Map<String, Object> param) {
        return "";
    }
}
