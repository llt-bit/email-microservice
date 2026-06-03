-- ============================================================
-- 内部邮件独立微服务 - MySQL 版本建表脚本
-- 从致远 OA Oracle 数据库剥离，转为 MySQL 独立数据库
-- ============================================================

-- 1. 邮件主体表（原 PRO_INMAIL_SUMMARY）
CREATE TABLE email_summary (
    id             BIGINT         NOT NULL COMMENT '邮件ID（雪花算法生成）',
    subject        VARCHAR(500)   DEFAULT NULL COMMENT '邮件标题',
    state          SMALLINT       DEFAULT 0 COMMENT '状态: 0=草稿 1=已发送 2=已删除 3=定时发送',
    sender_id      BIGINT         NOT NULL COMMENT '发送人ID',
    sender_name    VARCHAR(200)   DEFAULT NULL COMMENT '发送人名称(冗余)',
    org_department_id BIGINT      DEFAULT NULL COMMENT '发送人部门ID',
    org_account_id    BIGINT      DEFAULT NULL COMMENT '发送人单位ID',
    create_time    DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME       DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 收件人/抄送人（格式: "Member|id,Department|id,Account|id,Team|id"）
    send_to        TEXT           COMMENT '主送人名称列表',
    send_to_ids    TEXT           COMMENT '主送人ID列表(格式: Member|123,Department|456)',
    copy_to        TEXT           COMMENT '抄送人名称列表',
    copy_to_ids    TEXT           COMMENT '抄送人ID列表',

    -- 邮件正文
    summary_content LONGTEXT      COMMENT '邮件正文(HTML)',
    summary_size   BIGINT         DEFAULT 0 COMMENT '正文大小(字节)',

    -- 附件
    attachments_flag SMALLINT     DEFAULT 0 COMMENT '是否有附件: 0=无 1=有',
    attachments    TEXT           COMMENT '附件名称列表(冗余, 分隔符 /|)',

    -- 回复/转发相关
    mark           VARCHAR(255)   DEFAULT NULL COMMENT '邮箱标志: forward/reply/allReply/reEdit',
    reply_member   TEXT           COMMENT '回复人名称',
    reply_member_id BIGINT        DEFAULT NULL COMMENT '回复人ID',
    reply_parent_affair_id  BIGINT  DEFAULT NULL COMMENT '被回复邮件事项ID',
    reply_parent_summary_id BIGINT  DEFAULT NULL COMMENT '被回复邮件summaryId',
    forward_member  VARCHAR(255)  DEFAULT NULL COMMENT '转发人名称',
    forward_member_id BIGINT      DEFAULT NULL COMMENT '转发人ID',
    parentform_affair_id BIGINT   DEFAULT NULL COMMENT '被转发邮件事项ID',
    parentform_summary_id BIGINT  DEFAULT NULL COMMENT '被转发邮件summaryId',
    can_forward    SMALLINT       DEFAULT 1 COMMENT '是否可转发: 0=否 1=是',
    can_edit_attachment SMALLINT  DEFAULT 1 COMMENT '是否可编辑附件: 0=否 1=是',

    -- 定时发送
    is_timing      SMALLINT       DEFAULT 0 COMMENT '是否定时发送: 0=否 1=是',
    timing_date    DATETIME       DEFAULT NULL COMMENT '定时发送时间',

    -- 导出/跨网
    is_can_export  SMALLINT       DEFAULT 0 COMMENT '是否可导出: 0=否 1=是',
    kua_wang       SMALLINT       DEFAULT 0 COMMENT '是否跨网: 0=否 1=是',
    ferry_source   VARCHAR(100)   DEFAULT NULL COMMENT '数据摆渡来源',

    -- 密级/审批
    file_secret_level_id BIGINT   DEFAULT NULL COMMENT '文件密级ID',
    approver       TEXT           COMMENT '审批人ID列表',
    approver_str   TEXT           COMMENT '审批人名称列表',

    -- 自动保存草稿
    autosave       VARCHAR(50)    DEFAULT NULL COMMENT '自动保存标记',
    first_autosave_time DATETIME  DEFAULT NULL COMMENT '首次自动保存时间',

    -- 详情/描述
    start_detail   TEXT           COMMENT '发件人详情',
    send_to_detail TEXT           COMMENT '主送人详情',
    copy_to_detail TEXT           COMMENT '抄送人详情',

    -- 匿名投诉
    complaint_member_id BIGINT    DEFAULT NULL COMMENT '匿名投诉人ID',

    -- 短信内容（武汉金融项目）
    short_msg_content VARCHAR(1000) DEFAULT NULL COMMENT '短信通知内容',

    -- 创建人ID（审计）
    create_by      BIGINT         DEFAULT NULL COMMENT '创建人ID',

    PRIMARY KEY (id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_state (state),
    INDEX idx_create_time (create_time),
    INDEX idx_timing (is_timing, timing_date),
    INDEX idx_secret_level (file_secret_level_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件主体表';


-- 2. 邮件事务表（原 PRO_INMAIL_AFFAIR，一个收件人一条记录）
CREATE TABLE email_affair (
    id              BIGINT        NOT NULL COMMENT '事务ID',
    summary_id      BIGINT        NOT NULL COMMENT '关联邮件ID(email_summary.id)',
    member_id       BIGINT        NOT NULL COMMENT '收件人ID',
    member_name     VARCHAR(200)  DEFAULT NULL COMMENT '收件人名称(冗余)',
    sender_id       BIGINT        NOT NULL COMMENT '发件人ID',
    sender_name     VARCHAR(200)  DEFAULT NULL COMMENT '发件人名称(冗余)',
    subject         VARCHAR(500)  DEFAULT NULL COMMENT '邮件标题(冗余)',
    state           SMALLINT      DEFAULT NULL COMMENT '状态: 0=收件箱 1=草稿 2=已发送 3=已删除 5=加密箱',
    is_delete       SMALLINT      DEFAULT 0 COMMENT '是否彻底删除: 0=否 1=是',
    delete_state    SMALLINT      DEFAULT NULL COMMENT '删除前状态(用于恢复)',
    read_flag       SMALLINT      DEFAULT 0 COMMENT '是否已读: 0=未读 1=已读',
    browse_time     DATETIME      DEFAULT NULL COMMENT '阅读时间',
    is_handled      SMALLINT      DEFAULT 0 COMMENT '是否已办理: 0=否 1=是',
    collect         SMALLINT      DEFAULT 0 COMMENT '是否收藏: 0=否 1=是',
    attachments_flag SMALLINT     DEFAULT 0 COMMENT '是否有附件',
    affair_size     BIGINT        DEFAULT 0 COMMENT '邮件大小',
    is_forward      SMALLINT      DEFAULT 0 COMMENT '是否转发过',
    is_reply        SMALLINT      DEFAULT 0 COMMENT '是否回复过',
    reply_type      SMALLINT      DEFAULT NULL COMMENT '回复类型',
    pass_the_audit  SMALLINT      DEFAULT 0 COMMENT '审批是否通过: 0=未审批 1=已通过',
    path            VARCHAR(500)  DEFAULT NULL COMMENT '自定义文件夹路径',
    org_department_id BIGINT      DEFAULT NULL COMMENT '发件人部门ID',
    org_account_id  BIGINT        DEFAULT NULL COMMENT '发件人单位ID',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    forward_member  VARCHAR(255)  DEFAULT NULL COMMENT '转发人名称',
    PRIMARY KEY (id),
    INDEX idx_summary_id (summary_id),
    INDEX idx_member_id (member_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_state (state),
    INDEX idx_read_flag (read_flag),
    INDEX idx_collect (collect),
    INDEX idx_path (path(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件事务表(每个收件人一条)';


-- 3. 加密密码箱（原 PRO_INMAIL_SECRET）
CREATE TABLE email_secret (
    id            BIGINT        NOT NULL COMMENT 'ID',
    member_id     BIGINT        NOT NULL COMMENT '人员ID',
    login_name    VARCHAR(100)  DEFAULT NULL COMMENT '登录名',
    pwd           VARCHAR(500)  DEFAULT NULL COMMENT '加密密码(SM3/SHA哈希)',
    state         SMALLINT      DEFAULT NULL COMMENT '状态',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    ext1          VARCHAR(100)  DEFAULT NULL COMMENT '扩展字段1',
    ext2          VARCHAR(100)  DEFAULT NULL COMMENT '扩展字段2',
    ext3          VARCHAR(100)  DEFAULT NULL COMMENT '扩展字段3',
    ext4          VARCHAR(100)  DEFAULT NULL COMMENT '扩展字段4',
    ext5          VARCHAR(100)  DEFAULT NULL COMMENT '扩展字段5',
    PRIMARY KEY (id),
    INDEX idx_member_id (member_id),
    UNIQUE INDEX uk_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件加密密码箱';


-- 4. 邮件发送日志（原 PRO_INMAIL_LOG）
CREATE TABLE email_log (
    id               BIGINT        NOT NULL COMMENT 'ID',
    summary_id       BIGINT        DEFAULT NULL COMMENT '邮件ID',
    start_unit_ip    VARCHAR(100)  DEFAULT NULL COMMENT '发送方IP',
    send_to_unit_ids TEXT          COMMENT '主送单位ID列表',
    copy_to_unit_ids TEXT          COMMENT '抄送单位ID列表',
    attachment_name  LONGTEXT      COMMENT '附件名称列表',
    attachment_size  VARCHAR(100)  DEFAULT NULL COMMENT '附件大小',
    secret_id        VARCHAR(100)  DEFAULT NULL COMMENT '密级ID',
    is_file          SMALLINT      DEFAULT 0 COMMENT '是否含附件',
    is_log           SMALLINT      DEFAULT 0 COMMENT '是否日志',
    result           VARCHAR(100)  DEFAULT NULL COMMENT '发送结果',
    PRIMARY KEY (id),
    INDEX idx_summary_id (summary_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件发送日志';


-- 5. 自定义文件夹表（原 PRO_INMAIL_FOLDER）
CREATE TABLE email_folder (
    id          BIGINT        NOT NULL COMMENT '文件夹ID',
    member_id   BIGINT        NOT NULL COMMENT '所属人员ID',
    file_name   VARCHAR(255)  NOT NULL COMMENT '文件夹名称',
    path        VARCHAR(500)  NOT NULL COMMENT '文件夹完整路径',
    type        VARCHAR(100)  NOT NULL COMMENT '文件夹类型(inBox/sent)',
    is_delete   SMALLINT      DEFAULT 0 COMMENT '是否删除: 0=否 1=是',
    rule        TEXT          DEFAULT NULL COMMENT '过滤规则(JSON格式)',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_member_id (member_id),
    INDEX idx_path (path(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自定义文件夹';


-- 6. 群组/部门成员展开表（原 PRO_INMAIL_SUMMARY_MEMBERS）
CREATE TABLE email_summary_members (
    id              BIGINT        NOT NULL COMMENT 'ID',
    mail_summary_id BIGINT        NOT NULL COMMENT '邮件ID',
    member_id       BIGINT        DEFAULT NULL COMMENT '展开后的人员ID',
    member_name     VARCHAR(200)  DEFAULT NULL COMMENT '人员姓名',
    member_login_name VARCHAR(100) DEFAULT NULL COMMENT '人员登录名',
    type            VARCHAR(50)   DEFAULT NULL COMMENT '原始类型: Member/Department/Account/Team',
    type_id         BIGINT        DEFAULT NULL COMMENT '原始实体ID',
    detail          TEXT          DEFAULT NULL COMMENT '详情',
    account_id      BIGINT        DEFAULT NULL COMMENT '单位ID',
    sort            INT           DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (id),
    INDEX idx_summary_id (mail_summary_id),
    INDEX idx_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件群组/部门成员展开表';


-- 7. 跨网邮件追踪表（原 FORM_EMAIL_IS_ACROS_NETWORK）
CREATE TABLE email_across_network (
    id                BIGINT        NOT NULL COMMENT 'ID',
    source_target_id  BIGINT        DEFAULT NULL COMMENT '邮件ID',
    is_across_network SMALLINT      DEFAULT 0 COMMENT '是否跨网',
    db_tb             VARCHAR(100)  DEFAULT NULL COMMENT '源库表信息',
    create_time       DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_source_target (source_target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='跨网邮件追踪';


-- 8. 附件记录表（从 OA CTP_ATTACHMENT 剥离）
CREATE TABLE email_attachment (
    id            BIGINT        NOT NULL COMMENT '附件ID',
    summary_id    BIGINT        NOT NULL COMMENT '关联的邮件ID',
    file_name     VARCHAR(500)  NOT NULL COMMENT '原始文件名',
    file_size     BIGINT        DEFAULT 0 COMMENT '文件大小(字节)',
    mime_type     VARCHAR(200)  DEFAULT 'application/octet-stream' COMMENT 'MIME类型',
    minio_path    VARCHAR(500)  NOT NULL COMMENT 'MinIO存储路径',
    is_transfered SMALLINT      DEFAULT 0 COMMENT '是否已转换为预览格式',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    create_by     BIGINT        DEFAULT NULL COMMENT '上传人ID',
    PRIMARY KEY (id),
    INDEX idx_summary_id (summary_id),
    INDEX idx_minio_path (minio_path(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件附件记录';


-- 9. 密级审批表（独立自建，替代 OA CAP4 工作流）
CREATE TABLE email_approval (
    id                BIGINT        NOT NULL COMMENT '审批ID',
    summary_id        BIGINT        NOT NULL COMMENT '关联邮件ID',
    secret_level_id   BIGINT        DEFAULT NULL COMMENT '密级ID',
    secret_level_name VARCHAR(100)  DEFAULT NULL COMMENT '密级名称',
    status            VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '审批状态: PENDING/APPROVED/REJECTED',
    applicant_id      BIGINT        NOT NULL COMMENT '申请人ID',
    applicant_name    VARCHAR(200)  DEFAULT NULL COMMENT '申请人名称',
    create_time       DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    update_time       DATETIME      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '处理时间',
    PRIMARY KEY (id),
    INDEX idx_summary_id (summary_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件密级审批';


-- 10. 审批节点表
CREATE TABLE email_approval_node (
    id             BIGINT        NOT NULL COMMENT '节点ID',
    approval_id    BIGINT        NOT NULL COMMENT '审批ID',
    approver_id    BIGINT        NOT NULL COMMENT '审批人ID',
    approver_name  VARCHAR(200)  DEFAULT NULL COMMENT '审批人名称',
    status         VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '节点状态: PENDING/APPROVED/REJECTED',
    comment        TEXT          DEFAULT NULL COMMENT '审批意见',
    operate_time   DATETIME      DEFAULT NULL COMMENT '操作时间',
    sort           INT           DEFAULT 0 COMMENT '审批顺序',
    PRIMARY KEY (id),
    INDEX idx_approval_id (approval_id),
    INDEX idx_approver_id (approver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批节点';
