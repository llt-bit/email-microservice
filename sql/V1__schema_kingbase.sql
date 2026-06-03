-- ============================================================
-- 内部邮件独立微服务 - 人大金仓 KingbaseES V8R6 建表脚本
-- KingbaseES 兼容 PostgreSQL 语法
-- ============================================================

-- 1. 邮件主体表
CREATE TABLE email_summary (
    id             BIGINT         NOT NULL,
    subject        VARCHAR(500)   DEFAULT NULL,
    state          SMALLINT       DEFAULT 0,
    sender_id      BIGINT         NOT NULL,
    sender_name    VARCHAR(200)   DEFAULT NULL,
    org_department_id BIGINT      DEFAULT NULL,
    org_account_id    BIGINT      DEFAULT NULL,
    create_time    TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    update_time    TIMESTAMP      DEFAULT NULL,

    send_to        TEXT,
    send_to_ids    TEXT,
    copy_to        TEXT,
    copy_to_ids    TEXT,

    summary_content TEXT,
    summary_size   BIGINT         DEFAULT 0,
    attachments_flag SMALLINT     DEFAULT 0,
    attachments    TEXT,

    mark           VARCHAR(255)   DEFAULT NULL,
    reply_member   TEXT,
    reply_member_id BIGINT        DEFAULT NULL,
    reply_parent_affair_id  BIGINT  DEFAULT NULL,
    reply_parent_summary_id BIGINT  DEFAULT NULL,
    forward_member  VARCHAR(255)  DEFAULT NULL,
    forward_member_id BIGINT      DEFAULT NULL,
    parentform_affair_id BIGINT   DEFAULT NULL,
    parentform_summary_id BIGINT  DEFAULT NULL,
    can_forward    SMALLINT       DEFAULT 1,
    can_edit_attachment SMALLINT  DEFAULT 1,

    is_timing      SMALLINT       DEFAULT 0,
    timing_date    TIMESTAMP      DEFAULT NULL,

    is_can_export  SMALLINT       DEFAULT 0,
    kua_wang       SMALLINT       DEFAULT 0,
    ferry_source   VARCHAR(100)   DEFAULT NULL,

    file_secret_level_id BIGINT   DEFAULT NULL,
    approver       TEXT,
    approver_str   TEXT,

    autosave       VARCHAR(50)    DEFAULT NULL,
    first_autosave_time TIMESTAMP DEFAULT NULL,

    start_detail   TEXT,
    send_to_detail TEXT,
    copy_to_detail TEXT,

    complaint_member_id BIGINT    DEFAULT NULL,
    short_msg_content VARCHAR(1000) DEFAULT NULL,
    create_by      BIGINT         DEFAULT NULL,

    PRIMARY KEY (id)
);
CREATE INDEX idx_es_sender_id ON email_summary (sender_id);
CREATE INDEX idx_es_state ON email_summary (state);
CREATE INDEX idx_es_create_time ON email_summary (create_time);
CREATE INDEX idx_es_timing ON email_summary (is_timing, timing_date);
CREATE INDEX idx_es_secret_level ON email_summary (file_secret_level_id);
COMMENT ON TABLE email_summary IS '邮件主体表';
COMMENT ON COLUMN email_summary.state IS '状态: 0=草稿 1=已发送 2=已删除 3=定时发送';
COMMENT ON COLUMN email_summary.send_to_ids IS '主送人ID列表(格式: Member|123,Department|456)';

-- 2. 邮件事务表
CREATE TABLE email_affair (
    id              BIGINT        NOT NULL,
    summary_id      BIGINT        NOT NULL,
    member_id       BIGINT        NOT NULL,
    member_name     VARCHAR(200)  DEFAULT NULL,
    sender_id       BIGINT        NOT NULL,
    sender_name     VARCHAR(200)  DEFAULT NULL,
    subject         VARCHAR(500)  DEFAULT NULL,
    state           SMALLINT      DEFAULT NULL,
    is_delete       SMALLINT      DEFAULT 0,
    delete_state    SMALLINT      DEFAULT NULL,
    read_flag       SMALLINT      DEFAULT 0,
    browse_time     TIMESTAMP     DEFAULT NULL,
    is_handled      SMALLINT      DEFAULT 0,
    collect         SMALLINT      DEFAULT 0,
    attachments_flag SMALLINT     DEFAULT 0,
    affair_size     BIGINT        DEFAULT 0,
    is_forward      SMALLINT      DEFAULT 0,
    is_reply        SMALLINT      DEFAULT 0,
    reply_type      SMALLINT      DEFAULT NULL,
    pass_the_audit  SMALLINT      DEFAULT 0,
    path            VARCHAR(500)  DEFAULT NULL,
    org_department_id BIGINT      DEFAULT NULL,
    org_account_id  BIGINT        DEFAULT NULL,
    create_time     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP     DEFAULT NULL,
    forward_member  VARCHAR(255)  DEFAULT NULL,

    PRIMARY KEY (id)
);
CREATE INDEX idx_ea_summary_id ON email_affair (summary_id);
CREATE INDEX idx_ea_member_id ON email_affair (member_id);
CREATE INDEX idx_ea_sender_id ON email_affair (sender_id);
CREATE INDEX idx_ea_state ON email_affair (state);
CREATE INDEX idx_ea_read_flag ON email_affair (read_flag);
CREATE INDEX idx_ea_collect ON email_affair (collect);
CREATE INDEX idx_ea_path ON email_affair (path);
COMMENT ON TABLE email_affair IS '邮件事务表(每个收件人一条)';

-- 3. 加密密码箱
CREATE TABLE email_secret (
    id            BIGINT        NOT NULL,
    member_id     BIGINT        NOT NULL,
    login_name    VARCHAR(100)  DEFAULT NULL,
    pwd           VARCHAR(500)  DEFAULT NULL,
    state         SMALLINT      DEFAULT NULL,
    create_time   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP     DEFAULT NULL,
    ext1          VARCHAR(100)  DEFAULT NULL,
    ext2          VARCHAR(100)  DEFAULT NULL,
    ext3          VARCHAR(100)  DEFAULT NULL,
    ext4          VARCHAR(100)  DEFAULT NULL,
    ext5          VARCHAR(100)  DEFAULT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX idx_esec_member ON email_secret (member_id);
CREATE UNIQUE INDEX uk_esec_member ON email_secret (member_id);
COMMENT ON TABLE email_secret IS '邮件加密密码箱';

-- 4. 邮件发送日志
CREATE TABLE email_log (
    id               BIGINT        NOT NULL,
    summary_id       BIGINT        DEFAULT NULL,
    start_unit_ip    VARCHAR(100)  DEFAULT NULL,
    send_to_unit_ids TEXT,
    copy_to_unit_ids TEXT,
    attachment_name  TEXT,
    attachment_size  VARCHAR(100)  DEFAULT NULL,
    secret_id        VARCHAR(100)  DEFAULT NULL,
    is_file          SMALLINT      DEFAULT 0,
    is_log           SMALLINT      DEFAULT 0,
    result           VARCHAR(100)  DEFAULT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX idx_elog_summary ON email_log (summary_id);
COMMENT ON TABLE email_log IS '邮件发送日志';

-- 5. 自定义文件夹
CREATE TABLE email_folder (
    id          BIGINT        NOT NULL,
    member_id   BIGINT        NOT NULL,
    file_name   VARCHAR(255)  NOT NULL,
    path        VARCHAR(500)  NOT NULL,
    type        VARCHAR(100)  NOT NULL,
    is_delete   SMALLINT      DEFAULT 0,
    rule        TEXT          DEFAULT NULL,
    create_time TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE INDEX idx_ef_member ON email_folder (member_id);
CREATE INDEX idx_ef_path ON email_folder (path);
COMMENT ON TABLE email_folder IS '自定义文件夹';

-- 6. 群组/部门成员展开
CREATE TABLE email_summary_members (
    id              BIGINT        NOT NULL,
    mail_summary_id BIGINT        NOT NULL,
    member_id       BIGINT        DEFAULT NULL,
    member_name     VARCHAR(200)  DEFAULT NULL,
    member_login_name VARCHAR(100) DEFAULT NULL,
    type            VARCHAR(50)   DEFAULT NULL,
    type_id         BIGINT        DEFAULT NULL,
    detail          TEXT          DEFAULT NULL,
    account_id      BIGINT        DEFAULT NULL,
    sort            INT           DEFAULT 0,
    PRIMARY KEY (id)
);
CREATE INDEX idx_esm_summary ON email_summary_members (mail_summary_id);
CREATE INDEX idx_esm_member ON email_summary_members (member_id);
COMMENT ON TABLE email_summary_members IS '邮件群组/部门成员展开表';

-- 7. 跨网邮件追踪
CREATE TABLE email_across_network (
    id                BIGINT        NOT NULL,
    source_target_id  BIGINT        DEFAULT NULL,
    is_across_network SMALLINT      DEFAULT 0,
    db_tb             VARCHAR(100)  DEFAULT NULL,
    create_time       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE INDEX idx_ean_source ON email_across_network (source_target_id);
COMMENT ON TABLE email_across_network IS '跨网邮件追踪';

-- 8. 附件记录
CREATE TABLE email_attachment (
    id            BIGINT        NOT NULL,
    summary_id    BIGINT        NOT NULL,
    file_name     VARCHAR(500)  NOT NULL,
    file_size     BIGINT        DEFAULT 0,
    mime_type     VARCHAR(200)  DEFAULT 'application/octet-stream',
    minio_path    VARCHAR(500)  NOT NULL,
    is_transfered SMALLINT      DEFAULT 0,
    create_time   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    create_by     BIGINT        DEFAULT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX idx_eatt_summary ON email_attachment (summary_id);
CREATE INDEX idx_eatt_path ON email_attachment (minio_path);
COMMENT ON TABLE email_attachment IS '邮件附件记录';

-- 9. 密级审批
CREATE TABLE email_approval (
    id                BIGINT        NOT NULL,
    summary_id        BIGINT        NOT NULL,
    secret_level_id   BIGINT        DEFAULT NULL,
    secret_level_name VARCHAR(100)  DEFAULT NULL,
    status            VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    applicant_id      BIGINT        NOT NULL,
    applicant_name    VARCHAR(200)  DEFAULT NULL,
    create_time       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    update_time       TIMESTAMP     DEFAULT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX idx_eappr_summary ON email_approval (summary_id);
CREATE INDEX idx_eappr_status ON email_approval (status);
COMMENT ON TABLE email_approval IS '邮件密级审批';

-- 10. 审批节点
CREATE TABLE email_approval_node (
    id             BIGINT        NOT NULL,
    approval_id    BIGINT        NOT NULL,
    approver_id    BIGINT        NOT NULL,
    approver_name  VARCHAR(200)  DEFAULT NULL,
    status         VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    comment        TEXT          DEFAULT NULL,
    operate_time   TIMESTAMP     DEFAULT NULL,
    sort           INT           DEFAULT 0,
    PRIMARY KEY (id)
);
CREATE INDEX idx_eanode_approval ON email_approval_node (approval_id);
CREATE INDEX idx_eanode_approver ON email_approval_node (approver_id);
COMMENT ON TABLE email_approval_node IS '审批节点';
