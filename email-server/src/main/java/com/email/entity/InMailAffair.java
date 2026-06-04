package com.email.entity;

import java.sql.Timestamp;
import java.util.Date;

/**
 * 邮件事务 PO —— 从 OA InMailAffair 直接复制，使用 Hibernate hbm.xml 映射。
 */
public class InMailAffair {

    private Long id;
    private Long memberId;
    private Long senderId;
    private String subject;
    private Long objectId;
    private Integer state;
    private Boolean delFlag = false;
    private Date createDate;
    private Date updateDate;
    private Long orgDepartmentId;
    private Long orgAccountId;
    private Boolean readFlag = false;
    private Boolean attachmentsFlag = false;
    private Long size = 0L;
    private String forwardMember;
    private Boolean isForward = false;
    private Boolean isReply = false;
    private Integer replyType;
    private Timestamp browseTime;

    private String headImg;
    private String secretNameStr;
    private String secretIdStr;
    private boolean selec = false;
    private String path;
    private Integer passTheAudit;
    private Integer collect;
    private String security;
    private Integer deleteState;
    private Boolean isHandled;

    public void setIdIfNew() { if (this.id == null) this.id = com.email.platform.UUIDLong.longValue(); }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InMailAffair() {
        delFlag = false;
        readFlag = false;
        attachmentsFlag = false;
    }

    // ==================== getter/setter 从 OA 原样复制 ====================

    public boolean getSelec() { return selec; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getSecretNameStr() { return secretNameStr; }
    public String getSecretIdStr() { return secretIdStr; }
    public void setSecretNameStr(String secretNameStr) { this.secretNameStr = secretNameStr; }
    public void setSecretIdStr(String secretIdStr) { this.secretIdStr = secretIdStr; }
    public String getHeadImg() { return headImg; }
    public void setHeadImg(String headImg) { this.headImg = headImg; }
    public String getSecurity() { return security; }
    public void setSecurity(String security) { this.security = security; }

    public Boolean getIsReply() { return isReply; }
    public Timestamp getBrowseTime() { return browseTime; }
    public void setBrowseTime(Timestamp browseTime) { this.browseTime = browseTime; }
    public void setIsReply(Boolean isReply) { this.isReply = isReply; }
    public Integer getReplyType() { return replyType; }
    public void setReplyType(Integer replyType) { this.replyType = replyType; }
    public Boolean getIsForward() { return isForward; }
    public void setIsForward(Boolean isForward) { this.isForward = isForward; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public Long getOrgDepartmentId() { return orgDepartmentId; }
    public void setOrgDepartmentId(Long orgDepartmentId) { this.orgDepartmentId = orgDepartmentId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public Long getObjectId() { return objectId; }
    public void setObjectId(Long objectId) { this.objectId = objectId; }
    public Integer getState() { return state; }
    public void setState(Integer state) { this.state = state; }
    public Boolean getDelFlag() { return delFlag; }
    public void setDelFlag(Boolean delFlag) { this.delFlag = delFlag; }
    /** 兼容 OA 代码中的 getDelete()/setDelete() 调用 */
    public Boolean getDelete() { return delFlag; }
    public void setDelete(Boolean v) { this.delFlag = v; }
    public Date getCreateDate() { return createDate; }
    public void setCreateDate(Date createDate) { this.createDate = createDate; }
    public Date getUpdateDate() { return updateDate; }
    public void setUpdateDate(Date updateDate) { this.updateDate = updateDate; }
    public String getForwardMember() { return forwardMember; }
    public void setForwardMember(String forwardMember) { this.forwardMember = forwardMember; }
    public Long getOrgAccountId() { return orgAccountId; }
    public void setOrgAccountId(Long orgAccountId) { this.orgAccountId = orgAccountId; }
    public Boolean getReadFlag() { return readFlag; }
    public void setReadFlag(Boolean readFlag) { this.readFlag = readFlag; }
    public Boolean getAttachmentsFlag() { return attachmentsFlag; }
    public void setAttachmentsFlag(Boolean attachmentsFlag) { this.attachmentsFlag = attachmentsFlag; }
    public Integer getDeleteState() { return deleteState; }
    public void setDeleteState(Integer deleteState) { this.deleteState = deleteState; }
    public Boolean getIsHandled() { return isHandled; }
    public void setIsHandled(Boolean isHandled) { this.isHandled = isHandled; }
    public Integer getPassTheAudit() { return passTheAudit; }
    public void setPassTheAudit(Integer passTheAudit) { this.passTheAudit = passTheAudit; }
    public Integer getCollect() { return collect; }
    public void setCollect(Integer collect) { this.collect = collect; }
}
