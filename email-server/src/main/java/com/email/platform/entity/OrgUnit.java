package com.email.platform.entity;

/**
 * 组织单元实体 —— 映射 org_department 表。
 * HQL 中 "OrgUnit as ou" / "ou.id, ou.name" 对应 org_department 表。
 */
public class OrgUnit {
    private Long id;
    private String name;
    private Long parentId;
    private Long accountId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
}
