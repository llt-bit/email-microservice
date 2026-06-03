package com.email.platform;

import java.util.Map;

/** OA ParamUtil 兼容类 */
public class ParamUtil {
    public static String getString(Map map, String key) {
        if (map == null || key == null) return null;
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }
    public static String getString(Map map, String key, String def) {
        String v = getString(map, key);
        return v != null ? v : def;
    }
}
