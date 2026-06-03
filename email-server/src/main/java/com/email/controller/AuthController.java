package com.email.controller;

import com.email.common.R;
import com.email.entity.OrgMember;
import com.email.mapper.OrgMemberMapper;
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
 * <p>支持两种登录方式：
 * 1. 独立登录：用户输入OA用户名密码 → 调 OA 接口验证 → 签发 JWT
 * 2. SSO：OA 菜单点击时 OA 后端签发 JWT → 前端存储后调用 API（推荐）</p>
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource private JwtTokenProvider jwtTokenProvider;
    @Resource private OrgMemberMapper orgMemberMapper;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String loginName = params.get("loginName");
        String password = params.get("password");

        if (loginName == null || password == null) {
            return R.fail("用户名或密码为空");
        }

        // 在独立的邮件数据库中查找用户（需要先同步组织架构）
        OrgMember member = orgMemberMapper.findByLoginName(loginName);
        if (member == null) {
            return R.fail("用户不存在或未同步");
        }

        // TODO: 实际环境中需要调 OA 接口验证密码，这里暂时做简化验证
        // 生产环境中需要：
        // 1. 如果 OA 可用，调 OA REST 接口验证
        // 2. 如果 OA 不可用（宕机），用本地缓存的密码验证

        UserContext ctx = new UserContext();
        ctx.setUserId(member.getId());
        ctx.setLoginName(member.getLoginName());
        ctx.setUserName(member.getName());
        ctx.setDepartmentId(member.getDepartmentId());
        ctx.setAccountId(member.getAccountId());
        ctx.setAdmin(false);

        String token = jwtTokenProvider.generateToken(ctx);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("tokenType", "Bearer");
        result.put("userId", member.getId());
        result.put("loginName", member.getLoginName());
        result.put("userName", member.getName());
        result.put("departmentId", member.getDepartmentId());
        result.put("accountId", member.getAccountId());

        return R.ok(result);
    }

    @PostMapping("/sso/verify")
    public R<Map<String, Object>> ssoVerify(@RequestParam("token") String token) {
        // 已在 Filter 层完成验证并设置 UserContext
        UserContext ctx = UserContextHolder.get();

        Map<String, Object> result = new HashMap<>();
        result.put("userId", ctx.getUserId());
        result.put("loginName", ctx.getLoginName());
        result.put("userName", ctx.getUserName());
        return R.ok(result);
    }

    @PostMapping("/refresh")
    public R<Map<String, Object>> refresh() {
        UserContext ctx = UserContextHolder.get();
        String newToken = jwtTokenProvider.generateToken(ctx);

        Map<String, Object> result = new HashMap<>();
        result.put("token", newToken);
        return R.ok(result);
    }

    @GetMapping("/me")
    public R<Map<String, Object>> me() {
        UserContext ctx = UserContextHolder.get();
        Map<String, Object> result = new HashMap<>();
        result.put("userId", ctx.getUserId());
        result.put("loginName", ctx.getLoginName());
        result.put("userName", ctx.getUserName());
        result.put("departmentId", ctx.getDepartmentId());
        result.put("accountId", ctx.getAccountId());
        result.put("admin", ctx.isAdmin());
        return R.ok(result);
    }
}
