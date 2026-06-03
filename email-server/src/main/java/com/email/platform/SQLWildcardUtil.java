package com.email.platform;

/**
 * SQL LIKE 通配符转义 —— 对齐 OA com.seeyon.ctp.util.SQLWildcardUtil。
 */
public class SQLWildcardUtil {

    /**
     * 转义 LIKE 查询中的 % 和 _ 通配符。
     */
    public static String escape(String input) {
        if (input == null) return null;
        return input
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
