package com.email.entity;

/**
 * 邮件事务 BO —— 从 OA InMailAffairBO 直接复制。
 */
public class InMailAffairBO {
    private Long id;
    private Long summaryId;
    private Long memberId;
    private String img;
    private String subject;
    private String createDate;
    private Boolean readFlag = false;
    private Boolean attachmentsFlag = false;
    private String size = "0KB";
    private String senderName;
    private String recUserName;
    private String sendto;
    private String copyto;
    private int allTotal;
    private int notReadTotal;
    private String recUserDept;
    private String browseTime;
    private InMailAffair inMailAffair;
    private String security;
    private String nodeName;
    private String idStr, summaryIdStr, memberIdStr, senderIdStr;
    private String secretNameStr, secretIdStr;
    private String recipientName;
    private boolean selec;
    private Integer passTheAudit;
    private Integer collect;
    private Long senderId;
    private Boolean forwardFlag = false;
    private Boolean replyFlag = false;
    private Integer deleteState;
    private Boolean isHandled = false;

    public InMailAffairBO() { this.allTotal = 0; this.notReadTotal = 0; }
    public InMailAffairBO(InMailAffair a) { this(); this.inMailAffair = a; }

    // All getter/setter same as OA
    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public Long getSummaryId() { return summaryId; } public void setSummaryId(Long v) { this.summaryId = v; }
    public Long getMemberId() { return memberId; } public void setMemberId(Long v) { this.memberId = v; }
    public String getImg() { return img; } public void setImg(String v) { this.img = v; }
    public String getSubject() { return subject; } public void setSubject(String v) { this.subject = v; }
    public String getCreateDate() { return createDate; } public void setCreateDate(String v) { this.createDate = v; }
    public Boolean getReadFlag() { return readFlag; } public void setReadFlag(Boolean v) { this.readFlag = v; }
    public Boolean getAttachmentsFlag() { return attachmentsFlag; } public void setAttachmentsFlag(Boolean v) { this.attachmentsFlag = v; }
    public String getSize() { return size; } public void setSize(String v) { this.size = v; }
    public String getSenderName() { return senderName; } public void setSenderName(String v) { this.senderName = v; }
    public String getRecUserName() { return recUserName; } public void setRecUserName(String v) { this.recUserName = v; }
    public String getSendto() { return sendto; } public void setSendto(String v) { this.sendto = v; }
    public String getCopyto() { return copyto; } public void setCopyto(String v) { this.copyto = v; }
    public int getAllTotal() { return allTotal; } public void setAllTotal(int v) { this.allTotal = v; }
    public int getNotReadTotal() { return notReadTotal; } public void setNotReadTotal(int v) { this.notReadTotal = v; }
    public String getRecUserDept() { return recUserDept; } public void setRecUserDept(String v) { this.recUserDept = v; }
    public String getBrowseTime() { return browseTime; } public void setBrowseTime(String v) { this.browseTime = v; }
    public InMailAffair getInMailAffair() { return inMailAffair; } public void setInMailAffair(InMailAffair v) { this.inMailAffair = v; }
    public String getSecurity() { return security; } public void setSecurity(String v) { this.security = v; }
    public String getNodeName() { return nodeName; } public void setNodeName(String v) { this.nodeName = v; }
    public String getIdStr() { return idStr; } public void setIdStr(String v) { this.idStr = v; }
    public String getSummaryIdStr() { return summaryIdStr; } public void setSummaryIdStr(String v) { this.summaryIdStr = v; }
    public String getMemberIdStr() { return memberIdStr; } public void setMemberIdStr(String v) { this.memberIdStr = v; }
    public String getSenderIdStr() { return senderIdStr; } public void setSenderIdStr(String v) { this.senderIdStr = v; }
    public String getSecretNameStr() { return secretNameStr; } public void setSecretNameStr(String v) { this.secretNameStr = v; }
    public String getSecretIdStr() { return secretIdStr; } public void setSecretIdStr(String v) { this.secretIdStr = v; }
    public String getRecipientName() { return recipientName; } public void setRecipientName(String v) { this.recipientName = v; }
    public boolean getSelec() { return selec; } public void setSelec(boolean v) { this.selec = v; }
    public Integer getPassTheAudit() { return passTheAudit; } public void setPassTheAudit(Integer v) { this.passTheAudit = v; }
    public Integer getCollect() { return collect; } public void setCollect(Integer v) { this.collect = v; }
    public Boolean getForwardFlag() { return forwardFlag; } public void setForwardFlag(Boolean v) { this.forwardFlag = v; }
    public Boolean getReplyFlag() { return replyFlag; } public void setReplyFlag(Boolean v) { this.replyFlag = v; }
    public Long getSenderId() { return senderId; } public void setSenderId(Long v) { this.senderId = v; }
    public Integer getDeleteState() { return deleteState; } public void setDeleteState(Integer v) { this.deleteState = v; }
    public Boolean getIsHandled() { return isHandled; } public void setIsHandled(Boolean v) { this.isHandled = v; }
}
