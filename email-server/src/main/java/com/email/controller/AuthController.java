package com.email.controller;

import com.email.common.R;
import com.email.platform.DBAgent;
import com.email.platform.entity.OrgMember;
import com.email.security.JwtTokenProvider;
import com.email.security.UserContext;
import com.email.security.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证 API。
 *
 * <p>开发环境：admin/admin 直接登录。
 * 生产环境：调 OA 接口验证密码后签发 JWT（或通过 OA SSO 跳转传入 token）。</p>
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String loginName = params.get("loginName");
        String password = params.get("password");

        if (loginName == null || password == null) {
            return R.fail("用户名或密码为空");
        }

        // Hibernate 按登录名查询用户
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("loginName", loginName);
        java.util.List<?> result = DBAgent.find("FROM OrgMember WHERE loginName=:loginName AND enabled=1", queryParams);

        if (result.isEmpty()) {
            return R.fail("用户不存在或未同步");
        }
        OrgMember member = (OrgMember) result.get(0);

        // 开发环境：admin 密码为 "admin"
        if (!"admin".equals(password)) {
            return R.fail("用户名或密码错误");
        }

        UserContext ctx = new UserContext();
        ctx.setUserId(member.getId());
        ctx.setLoginName(member.getLoginName());
        ctx.setUserName(member.getName());
        ctx.setDepartmentId(member.getDepartmentId());
        ctx.setAccountId(member.getAccountId());
        ctx.setAdmin(false);

        String token = jwtTokenProvider.generateToken(ctx);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("token", token);
        resultMap.put("tokenType", "Bearer");
        resultMap.put("userId", member.getId());
        resultMap.put("loginName", member.getLoginName());
        resultMap.put("userName", member.getName());
        resultMap.put("departmentId", member.getDepartmentId());
        resultMap.put("accountId", member.getAccountId());

        return R.ok(resultMap);
    }

    @PostMapping("/refresh")
    public R<Map<String, Object>> refresh() {
        UserContext ctx = UserContextHolder.get();
        String newToken = jwtTokenProvider.generateToken(ctx);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("token", newToken);
        return R.ok(resultMap);
    }

    @GetMapping("/me")
    public R<Map<String, Object>> me() {
        UserContext ctx = UserContextHolder.get();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("userId", ctx.getUserId());
        resultMap.put("loginName", ctx.getLoginName());
        resultMap.put("userName", ctx.getUserName());
        resultMap.put("departmentId", ctx.getDepartmentId());
        resultMap.put("accountId", ctx.getAccountId());
        resultMap.put("admin", ctx.isAdmin());
        return R.ok(resultMap);
    }
}
