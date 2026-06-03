package com.email.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 群组/部门成员展开 PO —— 从 OA InMailSummaryMembersPo 直接复制。
 */
@TableName("email_summary_members")
public class InMailSummaryMembersPo extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("mail_summary_id")
    private Long mailSummaryId;
    @TableField("member_id")
    private Long memberId;
    @TableField("member_name")
    private String memberName;
    @TableField("member_login_name")
    private String memberLoginName;
    private String type;
    @TableField("type_id")
    private Long typeId;
    private String detail;
    @TableField("account_id")
    private Long accountId;
    private Integer sort;

    public Long getMailSummaryId() { return mailSummaryId; }
    public void setMailSummaryId(Long mailSummaryId) { this.mailSummaryId = mailSummaryId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getMemberLoginName() { return memberLoginName; }
    public void setMemberLoginName(String memberLoginName) { this.memberLoginName = memberLoginName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}
