package com.email.entity;

import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.util.Date;

public class InMailSummaryBO {
    private final String id;
    private final String summaryIdStr;
    private String subject;// 标题
    private final Integer state;// 状态0正常 1删除
    private final String startMemberId;// 发起人
    private final String orgDepartmentId;// 部门ID
    private final String orgAccountId;// 单位ID
    private final Date createDate;// 创建时间
    private final String forwardMember;// 转发人
    private final String forwardMemberId;// 转发人id
    private final String parentformSummaryid;// 转发父邮件ID
    private final String parentformAffairid;// 转发父邮件事项ID
    private Boolean canForward = false;// 是否能转发
    private Boolean attachmentsFlag = false;// 是否有附件
    private Boolean canEditAttachment = false;// 能否编辑附件
    private String size = "0";// 邮件大小

    private final String sendTo;// 主送
    private final String sendToIds;
    private String copyTo = "无";// 抄送
    private final String copyToIds;
    private final String content;// 正文

    private final String mark;// 标志forwordMail转发 reply 回复 allReply 全部回复

    private final String replyMember;// 回复人
    private final String replyMemberId;// 回复id
    private final String replyParentSummaryid;// 回复父邮件ID
    private final String replyParentAffairid;// 回复父邮件事项ID

    // xx所三网融合 【是否跨网】 wxt-yuanxinhuai
    private Boolean kuaWang = false;// 是否跨网
    // xx所三网融合 【是否可以导出】 wxt-yuanxinhuai
    private Boolean isCanExport = false;// 是否可以导出

    private final String receiversStr;
    private final String receivers;
    private final String copyReceiversStr;
    private final String copyReceivers;

    //电科xx所  yuanxinhuai 发送者信息  2021年8月16日 start
    //发送者信息
    private String startDetail;
    //主送者信息
    private String sendToDetail;
    //抄送者信息
    private String copyToDetail;
    // 审批者id
    private String approver;
    // 审批者姓名
    private String approverStr;
    //添加字段是否自动保存标识字段
    private String autosave;
    // 自动保存时写信界面打开时间
    private Timestamp firstAutosaveTime;
    // 是否定时发信
    private boolean timedTask;
    /* 客开 电科XX所 author：wxt shizhao 邮件增加发送人、主送人、抄送人 描述    2021.9.16   start */
    private String receiversDetail;
    // 发送时间
    private Timestamp timingDate;


    public InMailSummaryBO(InMailSummary inMailSummary) {
        this.id = inMailSummary.getId() != null ? inMailSummary.getId().toString() : "";
        subject = inMailSummary.getSubject();// 标题
        summaryIdStr = inMailSummary.getId() != null ? inMailSummary.getId().toString() : "";
        if (StringUtils.isNotBlank(subject)) {
            String identification = "";
            if ("reply".equals(inMailSummary.getMark()) || "allReply".equals(inMailSummary.getMark()))
                identification = "回复：";
            if ("forwordMail".equals(inMailSummary.getMark()))
                identification = "转发：";
            if (!subject.trim().startsWith(identification))
                subject = identification + subject;
        }
        state = inMailSummary.getState();// 状态0正常 1删除
        startMemberId = inMailSummary.getStartMemberId() != null ? inMailSummary.getStartMemberId().toString() : "";// 发起人
        orgDepartmentId = inMailSummary.getOrgDepartmentId() != null ? inMailSummary.getOrgDepartmentId().toString() : "";// 部门ID
        orgAccountId = inMailSummary.getOrgAccountId() != null ? inMailSummary.getOrgAccountId().toString() : "";// 单位ID
        createDate = inMailSummary.getCreateDate();// 创建时间
        forwardMember = inMailSummary.getForwardMember();// 转发人
        forwardMemberId = inMailSummary.getForwardMemberId() != null ? inMailSummary.getForwardMemberId().toString() : "";// 转发人id
        parentformSummaryid = inMailSummary.getParentformSummaryid() != null ? inMailSummary.getParentformSummaryid().toString() : "";// 转发父邮件ID
        parentformAffairid = inMailSummary.getParentformAffairid() != null ? inMailSummary.getParentformAffairid().toString() : "";// 转发父邮件事项ID
        canForward = inMailSummary.getCanForward();// 是否能转发
        attachmentsFlag = inMailSummary.getAttachmentsFlag();// 是否有附件
        canEditAttachment = inMailSummary.getCanEditAttachment();// 能否编辑附件
        size = inMailSummary.getSize() != null ? inMailSummary.getSize().toString() : "0";// 邮件大小
        sendTo = inMailSummary.getSendTo();// 主送
        sendToIds = inMailSummary.getSendToIds();
        copyTo = inMailSummary.getCopyTo();// 抄送
        copyToIds = inMailSummary.getCopyToIds();
        content = inMailSummary.getContent();// 正文
        mark = inMailSummary.getMark();// 标志forwordMail转发 reply 回复 allReply 全部回复
        replyMember = inMailSummary.getReplyMember();// 回复人
        replyMemberId = inMailSummary.getReplyMemberId() != null ? inMailSummary.getReplyMemberId().toString() : "";// 回复id
        replyParentSummaryid = inMailSummary.getReplyParentSummaryid() != null ? inMailSummary.getReplyParentSummaryid().toString() : "";// 回复父邮件ID
        replyParentAffairid = inMailSummary.getReplyParentAffairid() != null ? inMailSummary.getReplyParentAffairid().toString() : "";// 回复父邮件事项ID
        kuaWang = inMailSummary.getKuaWang();// 是否跨网
        isCanExport = inMailSummary.getIsCanExport();// 是否可以导出
        receiversStr = inMailSummary.getReceiversStr();
        receivers = inMailSummary.getReceivers();
        copyReceiversStr = inMailSummary.getCopyReceiversStr();
        copyReceivers = inMailSummary.getCopyReceivers();
        timedTask = inMailSummary.getTimedTask();
        timingDate = inMailSummary.getTimingDate();
        /* 客开 电科XX所 author：wxt shizhao 邮件增加发送人、主送人、抄送人 描述  2021.9.16 start   */
        this.startDetail = inMailSummary.getStartDetail();
        this.sendToDetail = inMailSummary.getSendToDetail();
        this.copyToDetail = inMailSummary.getCopyToDetail();
        this.receiversDetail = inMailSummary.getReceiversDetail();
        this.approver = inMailSummary.getApprover();
        this.approverStr = inMailSummary.getApproverStr();
        /* 客开 电科XX所 author：wxt shizhao 邮件增加发送人、主送人、抄送人 描述  2021.9.16 end   */
    }

    public String getStartDetail() {
        return startDetail;
    }

    public void setStartDetail(String startDetail) {
        this.startDetail = startDetail;
    }
    //电科xx所  yuanxinhuai 发送者信息  2021年8月16日 end

    public String getSendToDetail() {
        return sendToDetail;
    }

    public void setSendToDetail(String sendToDetail) {
        this.sendToDetail = sendToDetail;
    }
    /* 客开 电科XX所 author：wxt shizhao 邮件增加发送人、主送人、抄送人 描述    2021.9.16   end */

    public String getCopyToDetail() {
        return copyToDetail;
    }

    public void setCopyToDetail(String copyToDetail) {
        this.copyToDetail = copyToDetail;
    }


    /* 客开 电科XX所 author：wxt shizhao 邮件增加发送人、主送人、抄送人 描述  2021.9.16 start   */

    public String getReceiversDetail() {
        return receiversDetail;
    }

    public void setReceiversDetail(String receiversDetail) {
        this.receiversDetail = receiversDetail;
    }

    /* 客开 电科XX所 author：wxt shizhao 邮件增加发送人、主送人、抄送人 描述  2021.9.16 end   */
    public String getSummaryIdStr() {
        return summaryIdStr;
    }

    public String getsubject() {
        return subject;
    }

    public Integer getstate() {
        return state;
    }

    public String getstartMemberId() {
        return startMemberId;
    }

    public String getorgDepartmentId() {
        return orgDepartmentId;
    }

    public String getorgAccountId() {
        return orgAccountId;
    }

    public Date getcreateDate() {
        return createDate;
    }

    public String getforwardMember() {
        return forwardMember;
    }

    public String getForwardMemberId() {
        return forwardMemberId;
    }

    public String getparentformSummaryid() {
        return parentformSummaryid;
    }

    public String getParentformAffairid() {
        return parentformAffairid;
    }

    public Boolean getcanForward() {
        return canForward;
    }

    public Boolean getAttachmentsFlag() {
        return attachmentsFlag;
    }

    public Boolean getcanEditAttachment() {
        return canEditAttachment;
    }

    public String getSize() {
        return size;
    }

    public String getSendTo() {
        return sendTo;
    }

    public String getSendToIds() {
        return sendToIds;
    }

    public String getCopyTo() {
        return copyTo;
    }

    public String getCopyToIds() {
        return copyToIds;
    }

    public String getContent() {
        return content;
    }

    public String getMark() {
        return mark;
    }

    public String getReplyMember() {
        return replyMember;
    }

    public String getReplyMemberId() {
        return replyMemberId;
    }

    public String getReplyParentSummaryid() {
        return replyParentSummaryid;
    }

    public String getReplyParentAffairid() {
        return replyParentAffairid;
    }

    public Boolean getKuaWang() {
        return kuaWang;
    }

    public Boolean getIsCanExport() {
        return isCanExport;
    }

    public String getReceiversStr() {
        return receiversStr;
    }

    public String getReceivers() {
        return receivers;
    }

    public String getCopyReceiversStr() {
        return copyReceiversStr;
    }

    public String getCopyReceivers() {
        return copyReceivers;
    }

    public boolean isTimedTask() {
        return timedTask;
    }

    public void setTimedTask(boolean timedTask) {
        this.timedTask = timedTask;
    }

    public Timestamp getTimingDate() {
        return timingDate;
    }

    public void setTimingDate(Timestamp timingDate) {
        this.timingDate = timingDate;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public String getApproverStr() {
        return approverStr;
    }

    public void setApproverStr(String approverStr) {
        this.approverStr = approverStr;
    }

    public String getAutosave() {
        return autosave;
    }

    public void setAutosave(String autosave) {
        this.autosave = autosave;
    }

    public Timestamp getFirstAutosaveTime() {
        return firstAutosaveTime;
    }

    public void setFirstAutosaveTime(Timestamp firstAutosaveTime) {
        this.firstAutosaveTime = firstAutosaveTime;
    }
}
