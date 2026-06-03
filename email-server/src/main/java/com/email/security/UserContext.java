package com.email.security;

import lombok.Data;

/**
 * 当前登录用户上下文（替代 OA 的 AppContext.getCurrentUser() + User 对象）。
 *
 * <p>每次请求由 JwtAuthenticationFilter 从 Token 解析并注入 ThreadLocal。
 * 调用方式：UserContextHolder.get()</p>
 */
@Data
public class UserContext {
    /** OA 人员 ID（对应 V3xOrgMember.id） */
    private Long userId;
    /** 登录名 */
    private String loginName;
    /** 显示名称 */
    private String userName;
    /** 部门 ID */
    private Long departmentId;
    /** 单位 ID */
    private Long accountId;
    /** 是否 admin */
    private boolean admin;
}
