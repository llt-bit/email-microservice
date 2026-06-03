package com.email.entity;

/**
 * 群组/部门成员展开 PO —— 从 OA 直接复制，使用 Hibernate hbm.xml 映射。
 */
public class InMailSummaryMembersPo {

    private Long id;
    private Long mailSummaryId;
    private Long memberId;
    private String memberName;
    private String memberLoginName;
    private String type;
    private Long typeId;
    private String detail;
    private Long accountId;
    private Integer sort;

    public void setIdIfNew() { if (this.id == null) this.id = com.email.platform.UUIDLong.longValue(); }
    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public Long getMailSummaryId() { return mailSummaryId; } public void setMailSummaryId(Long v) { this.mailSummaryId = v; }
    public Long getMemberId() { return memberId; } public void setMemberId(Long v) { this.memberId = v; }
    public String getMemberName() { return memberName; } public void setMemberName(String v) { this.memberName = v; }
    public String getMemberLoginName() { return memberLoginName; } public void setMemberLoginName(String v) { this.memberLoginName = v; }
    public String getType() { return type; } public void setType(String v) { this.type = v; }
    public Long getTypeId() { return typeId; } public void setTypeId(Long v) { this.typeId = v; }
    public String getDetail() { return detail; } public void setDetail(String v) { this.detail = v; }
    public Long getAccountId() { return accountId; } public void setAccountId(Long v) { this.accountId = v; }
    public Integer getSort() { return sort; } public void setSort(Integer v) { this.sort = v; }
}
