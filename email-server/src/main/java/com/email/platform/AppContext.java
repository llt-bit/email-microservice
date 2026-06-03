package com.email.platform;

import com.email.security.UserContext;
import com.email.security.UserContextHolder;
import com.email.security.UserInfo;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * AppContext 兼容类 —— 对齐 OA com.seeyon.ctp.common.AppContext。
 *
 * <p>邮件业务代码中大量使用 AppContext 获取当前用户、查找 Spring Bean、读取系统配置。
 * 此类用 Spring ApplicationContextAware + ThreadLocal UserContext 实现一模一样的调用方式。</p>
 */
@Component
public class AppContext implements ApplicationContextAware {

    private static ApplicationContext springContext;
    private static Environment environment;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        springContext = ctx;
        environment = ctx.getEnvironment();
    }

    // ==================== Bean 查找 ====================

    public static Object getBean(String name) {
        if (springContext == null) return null;
        try {
            return springContext.getBean(name);
        } catch (Exception e) {
            return springContext.getBean(name, Object.class);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz) {
        return springContext != null ? springContext.getBean(clazz) : null;
    }

    // ==================== 当前用户 ====================

    /** 对齐 OA AppContext.getCurrentUser() */
    public static UserInfo getCurrentUser() {
        UserContext ctx = UserContextHolder.get();
        if (ctx == null) return null;
        UserInfo u = new UserInfo();
        u.setId(ctx.getUserId());
        u.setLoginName(ctx.getLoginName());
        u.setName(ctx.getUserName());
        u.setDepartmentId(ctx.getDepartmentId());
        u.setLoginAccount(ctx.getAccountId());
        return u;
    }

    /** 对齐 OA AppContext.currentUserId() */
    public static Long currentUserId() {
        UserInfo u = getCurrentUser();
        return u != null ? u.getId() : null;
    }

    /** 对齐 OA AppContext.currentAccountId() */
    public static Long currentAccountId() {
        UserInfo u = getCurrentUser();
        return u != null ? u.getLoginAccount() : null;
    }

    // ==================== 系统属性 ====================

    /** 对齐 OA AppContext.getSystemProperty(key) */
    public static String getSystemProperty(String key) {
        if (environment != null) {
            return environment.getProperty(key);
        }
        // 常用配置的默认值
        switch (key) {
            case "internalmail.mailSMCode":  return null;
            case "internalmail.mailKWCode":  return null;
            case "internalmail.mailIp":      return "127.0.0.1";
            case "datafusion.ip":            return "127.0.0.1";
            default: return null;
        }
    }
}
