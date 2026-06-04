package com.email.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 生成与验证。
 *
 * <p>支持两种 Token 来源：
 * 1. 从邮件微服务自身登录签发（方案B：独立登录）
 * 2. 从 OA 通过 SSO 密钥签发后传入（方案A：推荐）</p>
 */
@Component
public class JwtTokenProvider {
    private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(JwtTokenProvider.class);

    private final SecretKey key;
    private final SecretKey ssoKey;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${email.jwt.secret}") String secret,
            @Value("${email.jwt.sso-secret}") String ssoSecret,
            @Value("${email.jwt.expiration}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ssoKey = Keys.hmacShaKeyFor(ssoSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * 生成自身 JWT Token。
     */
    public String generateToken(UserContext userContext) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(userContext.getUserId()))
                .claim("loginName", userContext.getLoginName())
                .claim("userName", userContext.getUserName())
                .claim("deptId", userContext.getDepartmentId())
                .claim("acctId", userContext.getAccountId())
                .claim("admin", userContext.isAdmin())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * 验证自身签发的 Token。
     */
    public UserContext validateToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return buildUserContext(claims);
    }

    /**
     * 验证 OA SSO 签发的 Token（使用共享密钥）。
     *
     * 流程：用户在OA点击邮件菜单 → OA后端用 sso-secret 签发 JWT
     * → 重定向到邮件前端 URL?token=xxx → 前端带 token 调用邮件 API
     * → 邮件后端用 sso-secret 验证。
     */
    public UserContext validateSsoToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(ssoKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return buildUserContext(claims);
        } catch (JwtException e) {
            log.warn("OA SSO Token 验证失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 优先用自身密钥验证，失败再尝试 SSO 密钥。
     */
    public UserContext resolveToken(String token) {
        UserContext ctx = null;
        try {
            ctx = validateToken(token);
        } catch (JwtException ignored) {
            // 自身密钥失败，尝试 SSO
        }
        if (ctx == null) {
            ctx = validateSsoToken(token);
        }
        return ctx;
    }

    private UserContext buildUserContext(Claims claims) {
        UserContext ctx = new UserContext();
        ctx.setUserId(Long.parseLong(claims.getSubject()));
        ctx.setLoginName(claims.get("loginName", String.class));
        ctx.setUserName(claims.get("userName", String.class));
        Long deptId = claims.get("deptId", Long.class);
        ctx.setDepartmentId(deptId != null ? deptId : 0L);
        Long acctId = claims.get("acctId", Long.class);
        ctx.setAccountId(acctId != null ? acctId : 0L);
        Boolean admin = claims.get("admin", Boolean.class);
        ctx.setAdmin(admin != null && admin);
        return ctx;
    }
}
