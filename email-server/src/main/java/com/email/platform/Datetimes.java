package com.email.platform;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具 —— 对齐 OA com.seeyon.ctp.util.Datetimes。
 */
public class Datetimes {

    public static Timestamp getTimestamp(Date date) {
        if (date == null) return null;
        if (date instanceof Timestamp) return (Timestamp) date;
        return new Timestamp(date.getTime());
    }

    public static String format(Date date, String pattern) {
        if (date == null) return "";
        return new SimpleDateFormat(pattern).format(date);
    }

    /** OA Datetimes.getTodayFirstTime */
    public static java.util.Date getTodayFirstTime(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return new java.sql.Date(sdf.parse(dateStr).getTime());
        } catch (Exception e) { return new java.util.Date(); }
    }

    /** OA Datetimes.getTodayLastTime */
    public static java.util.Date getTodayLastTime(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return new Timestamp(sdf.parse(dateStr + " 23:59:59").getTime());
        } catch (Exception e) { return new java.util.Date(); }
    }

    /** OA Datetimes.getDateStr */
    public static String getDateStr(java.util.Date date, int type) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    /** OA Datetimes.formatDatetime —— 格式化日期时间 */
    public static String formatDatetime(java.util.Date date, int type) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}
