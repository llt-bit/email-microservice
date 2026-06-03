package com.email.security;

import lombok.Data;

/**
 * 替代 OA com.seeyon.ctp.common.authenticate.domain.User。
 * 保留与原 OA User 相同的方法签名，确保 InMailUtil 等工具类可逐字复制。
 */
@Data
public class UserInfo {
    private Long id;
    private String name;
    private String loginName;
    private Long departmentId;
    private Long loginAccount;
    private Long postId;
    private String remoteAddr;

    /** 从 UserContext 构建 */
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

    /** OA 代码中常用的构造器: new User(id, name, ...) */
    public UserInfo() {}
    public UserInfo(Long id, String name) { this.id = id; this.name = name; }
}
