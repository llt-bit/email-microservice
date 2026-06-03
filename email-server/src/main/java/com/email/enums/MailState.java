package com.email.enums;

import lombok.Getter;

/**
 * 邮件/事务状态枚举（从 OA InMailConstant 迁移）。
 *
 * <p>注意：OA 中 STATE 在 Summary 和 Affair 层级语义不同，这里统一管理。</p>
 */
@Getter
public enum MailState {

    /** 已发送（发件箱）- 注意 OA 中 ordinal=0 */
    SENT(0, "已发送"),
    /** 草稿 - OA 中 ordinal=1 */
    DRAFT(1, "草稿"),
    /** 收件箱 - OA 中 ordinal=2 */
    INBOX(2, "收件箱"),
    /** 已删除 */
    DELETED(3, "已删除"),
    /** 加密箱 */
    SECRET(5, "加密箱");

    private final int code;
    private final String label;

    MailState(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static MailState fromCode(int code) {
        for (MailState s : values()) {
            if (s.code == code) return s;
        }
        return null;
    }
}
