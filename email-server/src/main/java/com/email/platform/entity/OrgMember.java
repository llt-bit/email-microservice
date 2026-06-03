package com.email.platform.entity;

/**
 * 人员实体 —— Hibernate HQL JOIN 时需要。
 * 映射 org_member 表，供 HQL 中的 "OrgMember as s" 使用。
 */
public class OrgMember {
    private Long id;
    private String name;
    private String loginName;
    private Long departmentId;
    private Long accountId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLoginName() { return loginName; }
    public void setLoginName(String loginName) { this.loginName = loginName; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
}
