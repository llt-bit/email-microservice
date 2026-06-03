package com.email.platform;

import java.util.Collection;
import java.util.Iterator;

/**
 * 字符串工具 —— 对齐 OA com.seeyon.ctp.util.Strings。
 */
public class Strings {

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    public static boolean isBlank(Object s) {
        return s == null || s.toString().trim().isEmpty();
    }

    public static boolean isNotBlank(Object s) {
        return !isBlank(s);
    }

    public static String join(Collection<?> coll, String delimiter) {
        if (coll == null || coll.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        Iterator<?> it = coll.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) sb.append(delimiter);
        }
        return sb.toString();
    }

    public static String join(Object[] arr, String delimiter) {
        if (arr == null || arr.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) sb.append(delimiter);
        }
        return sb.toString();
    }

    public static String escape(String s) {
        if (s == null) return null;
        return s.replace("'", "''").replace("\\", "\\\\");
    }
}
