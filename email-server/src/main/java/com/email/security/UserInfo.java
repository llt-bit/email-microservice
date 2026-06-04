package com.email.security;

/**
 * 替代 OA com.seeyon.ctp.common.authenticate.domain.User。
 */
public class UserInfo {
    private Long id;
    private String name;
    private String loginName;
    private Long departmentId;
    private Long loginAccount;
    private Long postId;
    private String remoteAddr;

    public static UserInfo fromCurrentUser() {
        UserInfo u = new UserInfo();
        UserContext ctx = UserContextHolder.get();
        u.id = ctx.getUserId();
        u.loginName = ctx.getLoginName();
        u.name = ctx.getUserName();
        u.departmentId = ctx.getDepartmentId();
        u.loginAccount = ctx.getAccountId();
        return u;
    }

    public UserInfo() {}
    public UserInfo(Long id, String name) { this.id = id; this.name = name; }

    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public String getName() { return name; } public void setName(String v) { this.name = v; }
    public String getLoginName() { return loginName; } public void setLoginName(String v) { this.loginName = v; }
    public Long getDepartmentId() { return departmentId; } public void setDepartmentId(Long v) { this.departmentId = v; }
    public Long getLoginAccount() { return loginAccount; } public void setLoginAccount(Long v) { this.loginAccount = v; }
    public Long getPostId() { return postId; } public void setPostId(Long v) { this.postId = v; }
    public String getRemoteAddr() { return remoteAddr; } public void setRemoteAddr(String v) { this.remoteAddr = v; }
}
