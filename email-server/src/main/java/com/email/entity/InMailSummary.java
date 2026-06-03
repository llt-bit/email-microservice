package com.email.entity;

import java.sql.Timestamp;

/**
 * 邮件主体 PO —— 从 OA InMailSummary 直接复制，使用 Hibernate hbm.xml 映射。
 * 字段命名/getter/setter 与 OA 完全一致。
 */
public class InMailSummary {

    private Long id;
    private String _subject;
    private Integer _state;
    private Long _startMemberId;
    private Long _orgDepartmentId;
    private Long _orgAccountId;
    private Timestamp _createDate;
    private String _forwardMember;
    private Long forwardMemberId;
    private Long _parentformSummaryid;
    private Long parentformAffairid;
    private Boolean _canForward = false;
    private Boolean attachmentsFlag = false;
    private Boolean _canEditAttachment = false;
    private Long size = 0L;

    public String sendTo;
    public String sendToIds;
    public String copyTo = "无";
    public String copyToIds;
    public String content;

    private String mark;
    private String replyMember;
    private Long replyMemberId;
    private Long replyParentSummaryid;
    private Long replyParentAffairid;

    private Boolean kuaWang = false;
    private Boolean isCanExport = false;
    private String autosave;
    private Timestamp firstAutosaveTime;
    private String receiversStr;
    private String receivers;
    private String receiversDetail;
    private String copyReceiversStr;
    private String copyReceivers;
    private String startDetail;
    private String sendToDetail;
    private String copyToDetail;
    private boolean timedTask;
    private Timestamp timingDate;
    private InMailAffair inMailAffair;
    private String approver;
    private String approverStr;
    private String ferrySource;
    private Long complaintMemberId;
    private String attachments;
    private Long fileSecretLevelId;
    private String shortMsgContent;

    public void setIdIfNew() { if (this.id == null) this.id = com.email.platform.UUIDLong.longValue(); }

    public InMailSummary() {
        _canEditAttachment = false;
        _canForward = false;
    }

    // ======== getter/setter 从 OA 原样复制 ========
    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public String getAttachments() { return attachments; } public void setAttachments(String v) { this.attachments = v; }
    public InMailAffair getInMailAffair() { return inMailAffair; } public void setInMailAffair(InMailAffair v) { this.inMailAffair = v; }
    public String getReceiversDetail() { return receiversDetail; } public void setReceiversDetail(String v) { this.receiversDetail = v; }
    public String getReceiversStr() { receiversStr = sendTo; return receiversStr; }
    public String getReceivers() { receivers = sendToIds; return receivers; }
    public String getCopyReceiversStr() { copyReceiversStr = copyTo; return copyReceiversStr; }
    public String getCopyReceivers() { copyReceivers = copyToIds; return copyReceivers; }
    public String getStartDetail() { return startDetail; } public void setStartDetail(String v) { this.startDetail = v; }
    public String getSendToDetail() { return sendToDetail; } public void setSendToDetail(String v) { this.sendToDetail = v; }
    public String getCopyToDetail() { return copyToDetail; } public void setCopyToDetail(String v) { this.copyToDetail = v; }
    public void setCopyReceivers(String v) { this.copyReceivers = v; }
    public Long getFileSecretLevelId() { return fileSecretLevelId; } public void setFileSecretLevelId(Long v) { this.fileSecretLevelId = v; }
    public String getReplyMember() { return replyMember; } public void setReplyMember(String v) { this.replyMember = v; }
    public Long getReplyMemberId() { return replyMemberId; } public void setReplyMemberId(Long v) { this.replyMemberId = v; }
    public Long getReplyParentSummaryid() { return replyParentSummaryid; } public void setReplyParentSummaryid(Long v) { this.replyParentSummaryid = v; }
    public Long getReplyParentAffairid() { return replyParentAffairid; } public void setReplyParentAffairid(Long v) { this.replyParentAffairid = v; }
    public String getMark() { return mark; } public void setMark(String v) { this.mark = v; }
    public Long getParentformAffairid() { return parentformAffairid; } public void setParentformAffairid(Long v) { this.parentformAffairid = v; }
    public Long getSize() { return size; } public void setSize(Long v) { this.size = v; }
    public Long getForwardMemberId() { return forwardMemberId; } public void setForwardMemberId(Long v) { this.forwardMemberId = v; }
    public String getSendTo() { return sendTo; } public void setSendTo(String v) { this.sendTo = v; }
    public String getSendToIds() { return sendToIds; } public void setSendToIds(String v) { this.sendToIds = v; }
    public String getCopyTo() { return copyTo; } public void setCopyTo(String v) { this.copyTo = v; }
    public String getCopyToIds() { return copyToIds; } public void setCopyToIds(String v) { this.copyToIds = v; }
    public String getContent() { return content; } public void setContent(String v) { this.content = v; }
    public Boolean getAttachmentsFlag() { return attachmentsFlag; } public void setAttachmentsFlag(Boolean v) { this.attachmentsFlag = v; }
    public String getSubject() { return _subject; } public void setSubject(String v) { this._subject = v; }
    public Integer getState() { return _state; } public void setState(Integer v) { this._state = v; }
    public String getForwardMember() { return _forwardMember; } public void setForwardMember(String v) { this._forwardMember = v; }
    public Long getParentformSummaryid() { return _parentformSummaryid; } public void setParentformSummaryid(Long v) { this._parentformSummaryid = v; }
    public Long getStartMemberId() { return _startMemberId; } public void setStartMemberId(Long v) { this._startMemberId = v; }
    public Boolean getCanEditAttachment() { return _canEditAttachment; } public void setCanEditAttachment(Boolean v) { this._canEditAttachment = v; }
    public Long getOrgAccountId() { return _orgAccountId; } public void setOrgAccountId(Long v) { this._orgAccountId = v; }
    public java.util.Date getCreateDate() { return _createDate; } public void setCreateDate(Timestamp v) { this._createDate = v; }
    public Long getOrgDepartmentId() { return _orgDepartmentId; } public void setOrgDepartmentId(Long v) { this._orgDepartmentId = v; }
    public Boolean getCanForward() { return _canForward; } public void setCanForward(Boolean v) { this._canForward = v; }
    public String getShortMsgContent() { return shortMsgContent; } public void setShortMsgContent(String v) { this.shortMsgContent = v; }
    public Boolean getKuaWang() { return kuaWang; } public void setKuaWang(Boolean v) { this.kuaWang = v; }
    public Boolean getIsCanExport() { return isCanExport; } public void setIsCanExport(Boolean v) { this.isCanExport = v; }
    public boolean getTimedTask() { return timedTask; } public void setTimedTask(boolean v) { this.timedTask = v; }
    public Timestamp getTimingDate() { return timingDate; } public void setTimingDate(Timestamp v) { this.timingDate = v; }
    public String getApprover() { return approver; } public void setApprover(String v) { this.approver = v; }
    public String getApproverStr() { return approverStr; } public void setApproverStr(String v) { this.approverStr = v; }
    public String getAutosave() { return autosave; } public void setAutosave(String v) { this.autosave = v; }
    public Timestamp getFirstAutosaveTime() { return firstAutosaveTime; } public void setFirstAutosaveTime(Timestamp v) { this.firstAutosaveTime = v; }
    public String getFerrySource() { return ferrySource; } public void setFerrySource(String v) { this.ferrySource = v; }
    public Long getComplaintMemberId() { return complaintMemberId; } public void setComplaintMemberId(Long v) { this.complaintMemberId = v; }
}
