package com.email.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结构。
 *
 * <p>注意：为兼容原 OA 前端，保留 {@code code} 与 {@code msg} 字段语义。
 * 原 OA 返回的 code 多为字符串（如 "00010001" 表示成功），
 * 这里统一为：success=true/false + code(数字) + msg + data。
 * 前端改造时统一适配新结构。</p>
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 是否成功 */
    private boolean success;
    /** 业务码：0=成功，非0=失败 */
    private int code;
    /** 提示信息 */
    private String msg;
    /** 数据载荷 */
    private T data;

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.success = true;
        r.code = 0;
        r.msg = "操作成功";
        r.data = data;
        return r;
    }

    public static <T> R<T> ok(String msg, T data) {
        R<T> r = ok(data);
        r.msg = msg;
        return r;
    }

    public static <T> R<T> fail(String msg) {
        return fail(500, msg);
    }

    public static <T> R<T> fail(int code, String msg) {
        R<T> r = new R<>();
        r.success = false;
        r.code = code;
        r.msg = msg;
        return r;
    }
}
