package com.email.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.sql.Timestamp;

/**
 * 邮件主体 PO —— 从 OA InMailSummary 直接复制，只替换 BasePO→BaseEntity，加 MyBatis 注解。
 * 字段命名、getter/setter 与原 OA 完全一致，确保业务代码无需修改。
 */
@TableName("email_summary")
public class InMailSummary extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String _subject;           // 标题
    @TableField("state")
    private Integer _state;            // 状态0正常 1删除
    @TableField("sender_id")
    private Long _startMemberId;       // 发起人
    private Long _orgDepartmentId;     // 部门ID
    private Long _orgAccountId;        // 单位ID
    @TableField("create_time")
    private Timestamp _createDate;     // 创建时间
    private String _forwardMember;     // 转发人
    private Long forwardMemberId;      // 转发人id
    @TableField("parentform_summary_id")
    private Long _parentformSummaryid; // 转发父邮件ID
    private Long parentformAffairid;   // 转发父邮件事项ID
    private Boolean _canForward = false;
    private Boolean attachmentsFlag = false;
    private Boolean _canEditAttachment = false;
    private Long size = 0L;

    @TableField("send_to")
    public String sendTo;             // 主送
    @TableField("send_to_ids")
    public String sendToIds;
    @TableField("copy_to")
    public String copyTo = "无";      // 抄送
    @TableField("copy_to_ids")
    public String copyToIds;
    @TableField("summary_content")
    public String content;            // 正文

    private String mark;
    private String replyMember;       // 回复人
    private Long replyMemberId;
    @TableField("reply_parent_summary_id")
    private Long replyParentSummaryid;
    @TableField("reply_parent_affair_id")
    private Long replyParentAffairid;

    private Boolean kuaWang = false;
    private Boolean isCanExport = false;
    private String autosave;
    @TableField("first_autosave_time")
    private Timestamp firstAutosaveTime;
    private String receiversStr;
    private String receivers;
    private String receiversDetail;
    private String copyReceiversStr;
    private String copyReceivers;
    private String startDetail;
    @TableField("send_to_detail")
    private String sendToDetail;
    @TableField("copy_to_detail")
    private String copyToDetail;
    private boolean timedTask;
    @TableField("timing_date")
    private Timestamp timingDate;
    @TableField(exist = false)
    private InMailAffair inMailAffair;
    private String approver;
    @TableField("approver_str")
    private String approverStr;
    @TableField("ferry_source")
    private String ferrySource;
    @TableField("complaint_member_id")
    private Long complaintMemberId;
    private String attachments;
    @TableField("file_secret_level_id")
    private Long fileSecretLevelId;
    @TableField("short_msg_content")
    private String shortMsgContent;

    // ==================== 以下从 OA PO 原样复制 ====================

    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }

    public InMailAffair getInMailAffair() { return inMailAffair; }
    public void setInMailAffair(InMailAffair inMailAffair) { this.inMailAffair = inMailAffair; }

    public String getReceiversDetail() { return receiversDetail; }
    public void setReceiversDetail(String receiversDetail) { this.receiversDetail = receiversDetail; }

    public String getReceiversStr() { receiversStr = sendTo; return receiversStr; }
    public String getReceivers() { receivers = sendToIds; return receivers; }
    public String getCopyReceiversStr() { copyReceiversStr = copyTo; return copyReceiversStr; }
    public String getCopyReceivers() { copyReceivers = copyToIds; return copyReceivers; }

    public String getStartDetail() { return startDetail; }
    public void setStartDetail(String startDetail) { this.startDetail = startDetail; }
    public String getSendToDetail() { return sendToDetail; }
    public void setSendToDetail(String sendToDetail) { this.sendToDetail = sendToDetail; }
    public String getCopyToDetail() { return copyToDetail; }
    public void setCopyToDetail(String copyToDetail) { this.copyToDetail = copyToDetail; }
    public void setCopyReceivers(String copyReceivers) { this.copyReceivers = copyReceivers; }

    public Long getFileSecretLevelId() { return fileSecretLevelId; }
    public void setFileSecretLevelId(Long fileSecretLevelId) { this.fileSecretLevelId = fileSecretLevelId; }

    public InMailSummary() {
        _canEditAttachment = Boolean.valueOf(false);
        _canForward = Boolean.valueOf(false);
    }

    public String getReplyMember() { return replyMember; }
    public void setReplyMember(String replyMember) { this.replyMember = replyMember; }
    public Long getReplyMemberId() { return replyMemberId; }
    public void setReplyMemberId(Long replyMemberId) { this.replyMemberId = replyMemberId; }
    public Long getReplyParentSummaryid() { return replyParentSummaryid; }
    public void setReplyParentSummaryid(Long replyParentSummaryid) { this.replyParentSummaryid = replyParentSummaryid; }
    public Long getReplyParentAffairid() { return replyParentAffairid; }
    public void setReplyParentAffairid(Long replyParentAffairid) { this.replyParentAffairid = replyParentAffairid; }
    public String getMark() { return mark; }
    public Long getParentformAffairid() { return parentformAffairid; }
    public void setParentformAffairid(Long parentformAffairid) { this.parentformAffairid = parentformAffairid; }
    public void setMark(String mark) { this.mark = mark; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public Long getForwardMemberId() { return forwardMemberId; }
    public void setForwardMemberId(Long forwardMemberId) { this.forwardMemberId = forwardMemberId; }
    public String getSendTo() { return sendTo; }
    public void setSendTo(String sendTo) { this.sendTo = sendTo; }
    public String getSendToIds() { return sendToIds; }
    public void setSendToIds(String sendToIds) { this.sendToIds = sendToIds; }
    public String getCopyTo() { return copyTo; }
    public void setCopyTo(String copyTo) { this.copyTo = copyTo; }
    public String getCopyToIds() { return copyToIds; }
    public void setCopyToIds(String copyToIds) { this.copyToIds = copyToIds; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Boolean getAttachmentsFlag() { return attachmentsFlag; }
    public void setAttachmentsFlag(Boolean attachmentsFlag) { this.attachmentsFlag = attachmentsFlag; }
    public String getSubject() { return _subject; }
    public void setSubject(String _subject) { this._subject = _subject; }
    public Integer getState() { return _state; }
    public void setState(Integer _state) { this._state = _state; }
    public String getForwardMember() { return _forwardMember; }
    public void setForwardMember(String _forwardMember) { this._forwardMember = _forwardMember; }
    public Long getParentformSummaryid() { return _parentformSummaryid; }
    public void setParentformSummaryid(Long _parentformSummaryid) { this._parentformSummaryid = _parentformSummaryid; }
    public Long getStartMemberId() { return _startMemberId; }
    public void setStartMemberId(Long _startMemberId) { this._startMemberId = _startMemberId; }
    public Boolean getCanEditAttachment() { return _canEditAttachment; }
    public void setCanEditAttachment(Boolean _canEditAttachment) { this._canEditAttachment = _canEditAttachment; }
    public Long getOrgAccountId() { return _orgAccountId; }
    public void setOrgAccountId(Long _orgAccountId) { this._orgAccountId = _orgAccountId; }
    public java.util.Date getCreateDate() { return _createDate; }
    public void setCreateDate(Timestamp _createDate) { this._createDate = _createDate; }
    public Long getOrgDepartmentId() { return _orgDepartmentId; }
    public void setOrgDepartmentId(Long _orgDepartmentId) { this._orgDepartmentId = _orgDepartmentId; }
    public Boolean getCanForward() { return _canForward; }
    public void setCanForward(Boolean _canForward) { this._canForward = _canForward; }
    public String getShortMsgContent() { return shortMsgContent; }
    public void setShortMsgContent(String shortMsgContent) { this.shortMsgContent = shortMsgContent; }
    public Boolean getKuaWang() { return kuaWang; }
    public void setKuaWang(Boolean kuaWang) { this.kuaWang = kuaWang; }
    public Boolean getIsCanExport() { return isCanExport; }
    public void setIsCanExport(Boolean isCanExport) { this.isCanExport = isCanExport; }
    public boolean getTimedTask() { return timedTask; }
    public void setTimedTask(boolean timedTask) { this.timedTask = timedTask; }
    public Timestamp getTimingDate() { return timingDate; }
    public void setTimingDate(Timestamp timingDate) { this.timingDate = timingDate; }
    public String getApprover() { return approver; }
    public void setApprover(String approver) { this.approver = approver; }
    public String getApproverStr() { return approverStr; }
    public void setApproverStr(String approverStr) { this.approverStr = approverStr; }
    public String getAutosave() { return autosave; }
    public void setAutosave(String autosave) { this.autosave = autosave; }
    public Timestamp getFirstAutosaveTime() { return firstAutosaveTime; }
    public void setFirstAutosaveTime(Timestamp firstAutosaveTime) { this.firstAutosaveTime = firstAutosaveTime; }
    public String getFerrySource() { return ferrySource; }
    public void setFerrySource(String ferrySource) { this.ferrySource = ferrySource; }
    public Long getComplaintMemberId() { return complaintMemberId; }
    public void setComplaintMemberId(Long complaintMemberId) { this.complaintMemberId = complaintMemberId; }
}
