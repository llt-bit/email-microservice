package com.email.security;

/**
 * ThreadLocal 持有当前请求的用户上下文。
 */
public final class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT = new InheritableThreadLocal<>();

    private UserContextHolder() {}

    public static void set(UserContext ctx) {
        CONTEXT.set(ctx);
    }

    public static UserContext get() {
        UserContext ctx = CONTEXT.get();
        if (ctx == null) {
            throw new RuntimeException("用户未登录或上下文丢失");
        }
        return ctx;
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static Long getUserId() {
        return get().getUserId();
    }

    public static String getLoginName() {
        return get().getLoginName();
    }
}
