package com.email.security;

/**
 * 当前登录用户上下文（替代 OA 的 AppContext.getCurrentUser() + User 对象）。
 */
public class UserContext {
    private Long userId;
    private String loginName;
    private String userName;
    private Long departmentId;
    private Long accountId;
    private boolean admin;

    public Long getUserId() { return userId; } public void setUserId(Long v) { this.userId = v; }
    public String getLoginName() { return loginName; } public void setLoginName(String v) { this.loginName = v; }
    public String getUserName() { return userName; } public void setUserName(String v) { this.userName = v; }
    public Long getDepartmentId() { return departmentId; } public void setDepartmentId(Long v) { this.departmentId = v; }
    public Long getAccountId() { return accountId; } public void setAccountId(Long v) { this.accountId = v; }
    public boolean isAdmin() { return admin; } public void setAdmin(boolean v) { this.admin = v; }
}
