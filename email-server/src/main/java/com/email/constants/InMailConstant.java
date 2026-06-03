package com.email.constants;

/**
 * 邮件常量枚举 —— 从 OA InMailConstant 逐字复制。
 * ordinal 值必须在所有代码搬迁完成后与 OA 完全一致。
 */
public class InMailConstant {

    public enum InMailAffairState {
        sent,      // 0 - 已发送
        draf,      // 1 - 草稿
        run,       // 2 - 收件箱
        deleted,   // 3 - 已删除
        rescind,   // 4 - 撤销
        secret,    // 5 - 加密
        notSecret  // 6 - 全部非加密
    }

    public enum InMailSummaryState {
        run,       // 0 - 正常
        deleted,   // 1 - 已删除
        secret     // 2 - 加密
    }

    public enum InMailReplyType {
        all,       // 0 - 全部回复
        reply      // 1 - 仅回复发件人
    }

    public enum InMailSummaryMembersType {
        NULL,      // 0
        Team,      // 1 - 组
        Department,// 2 - 部门
        Account,   // 3 - 单位
        Post       // 4 - 岗位
    }
}
