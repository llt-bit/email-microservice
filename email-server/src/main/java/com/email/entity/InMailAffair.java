package com.email.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.sql.Timestamp;
import java.util.Date;

/**
 * 邮件事务 PO —— 从 OA InMailAffair 直接复制。
 * 去掉 extends BasePO, CtpAffair 引用，其余完全一致。
 */
@TableName("email_affair")
public class InMailAffair extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long memberId;                              // 人员id
    private Long senderId;                              // 发起者id
    private String subject;                             // 标题
    @TableField("summary_id")
    private Long objectId;                              // 内部邮件主表id
    private Integer state;                              // 状态
    @TableField("is_delete")
    private Boolean delete = false;                     // 是否删除
    @TableField("create_time")
    private Date createDate;                            // 创建日期
    @TableField("update_time")
    private Date updateDate;                            // 更新日期
    private Long orgDepartmentId;                       // 部门ID
    private Long orgAccountId;                          // 单位id
    @TableField("read_flag")
    private Boolean readFlag = false;                   // 已浏览
    @TableField("attachments_flag")
    private Boolean attachmentsFlag = false;            // 是否有附件
    @TableField("affair_size")
    private Long size = 0L;                             // 邮件大小
    @TableField("forward_member")
    private String forwardMember;                       // 转发人名称
    @TableField("is_forward")
    private Boolean isForward = false;                  // 是否已转发
    @TableField("is_reply")
    private Boolean isReply = false;                    // 是否已回复
    private Integer replyType;                          // 回复类型
    @TableField("browse_time")
    private Timestamp browseTime;                       // 浏览时间

    @TableField(exist = false)
    private String headImg;
    @TableField(exist = false)
    private String secretNameStr;
    @TableField(exist = false)
    private String secretIdStr;
    @TableField(exist = false)
    private boolean selec = false;
    private String path;
    @TableField("pass_the_audit")
    private Integer passTheAudit;                       // 涉密文件是否审核通过
    private Integer collect;                            // 是否收藏
    @TableField(exist = false)
    private String security;

    @TableField("delete_state")
    private Integer deleteState;                        // 删除时的状态
    @TableField("is_handled")
    private Boolean isHandled;                          // 是否手动置为已办理

    public InMailAffair() {
        delete = false;
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
    public Boolean getDelete() { return delete; }
    public void setDelete(Boolean delete) { this.delete = delete; }
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
