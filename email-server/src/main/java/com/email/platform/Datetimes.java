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
}
