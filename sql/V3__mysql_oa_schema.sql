-- ============================================================
-- OA 原表结构 → MySQL 版本
-- 表名、列名、类型完全对标 OA Oracle 定义
-- 目的：hbm.xml 可直接复用，HQL 原样跑，Manager/DAO 逐字复制
-- ============================================================

-- 先删之前建错的 email_* 表
DROP TABLE IF EXISTS email_summary_members;
DROP TABLE IF EXISTS email_attachment;
DROP TABLE IF EXISTS email_across_network;
DROP TABLE IF EXISTS email_approval_node;
DROP TABLE IF EXISTS email_approval;
DROP TABLE IF EXISTS email_log;
DROP TABLE IF EXISTS email_folder;
DROP TABLE IF EXISTS email_secret;
DROP TABLE IF EXISTS email_affair;
DROP TABLE IF EXISTS email_summary;

-- ============================================================
-- 1. 邮件主表（对标 OA PRO_INMAIL_SUMMARY + ALTER 补充字段）
-- ============================================================
CREATE TABLE pro_inmail_summary (
    ID                     BIGINT       NOT NULL COMMENT '编号',
    SUBJECT                VARCHAR(500) DEFAULT NULL COMMENT '标题',
    STATE                  INT          DEFAULT NULL COMMENT '状态: 0=正常(run)',
    START_MEMBER_ID        BIGINT       DEFAULT NULL COMMENT '发送者id',
    ORG_DEPARTMENT_ID      BIGINT       DEFAULT NULL COMMENT '部门id',
    ORG_ACCOUNT_ID         BIGINT       DEFAULT NULL COMMENT '单位id',
    CREATE_DATE            DATETIME     DEFAULT NULL COMMENT '创建时间',
    FORWARD_MEMBER         VARCHAR(255) DEFAULT NULL COMMENT '转发人名称',
    FORWARD_MEMBER_ID      BIGINT       DEFAULT NULL COMMENT '转发人id',
    PARENTFORM_AFFAIRID    BIGINT       DEFAULT NULL COMMENT '转发邮件事项id',
    PARENTFORM_SUMMARYID   BIGINT       DEFAULT NULL COMMENT '转发邮件id',
    CAN_FORWARD            INT          DEFAULT NULL COMMENT '是否可转发',
    ATTACHMENTS_FLAG       INT          DEFAULT NULL COMMENT '是否有附件',
    CAN_EDIT_ATTACHMENT    INT          DEFAULT NULL COMMENT '是否可编辑附件',
    SEND_TO                TEXT         DEFAULT NULL COMMENT '收件人',
    SEND_TO_IDS            TEXT         DEFAULT NULL COMMENT '收件人IDs',
    COPY_TO                TEXT         DEFAULT NULL COMMENT '抄送人',
    COPY_TO_IDS            TEXT         DEFAULT NULL COMMENT '抄送人IDs',
    SUMMARY_CONTENT        LONGTEXT     DEFAULT NULL COMMENT '正文',
    SUMMARY_SIZE           BIGINT       DEFAULT NULL COMMENT '大小',
    MARK                   VARCHAR(255) DEFAULT NULL COMMENT '标志: forward/reply/allReply',
    REPLY_MEMBER           TEXT         DEFAULT NULL COMMENT '回复人名称',
    REPLY_MEMBER_ID        BIGINT       DEFAULT NULL COMMENT '回复人id',
    REPLY_PARENT_AFFAIRID  BIGINT       DEFAULT NULL COMMENT '回复父邮件事项id',
    REPLY_PARENT_SUMMARYID BIGINT       DEFAULT NULL COMMENT '回复父邮件id',
    SHORTMSG_CONTENT       VARCHAR(1000) DEFAULT NULL COMMENT '短信内容',

    -- ALTER 补充字段
    KUA_WANG               INT          DEFAULT 0 COMMENT '是否跨网 0=否 1=是',
    isCanExport            INT          DEFAULT 0 COMMENT '是否可导出',
    IS_TIMING              INT          DEFAULT 0 COMMENT '是否定时发送',
    TIMING_Date            DATETIME     DEFAULT NULL COMMENT '定时发送时间',
    START_DETAIL           TEXT         DEFAULT NULL COMMENT '发件人详情',
    SEND_TO_DETAIL         TEXT         DEFAULT NULL COMMENT '主送人详情',
    COPY_TO_DETAIL         TEXT         DEFAULT NULL COMMENT '抄送人详情',
    APPROVER               TEXT         DEFAULT NULL COMMENT '审批人IDs',
    APPROVERSTR            TEXT         DEFAULT NULL COMMENT '审批人名称',
    AUTOSAVE               VARCHAR(50)  DEFAULT NULL COMMENT '自动保存标记',
    FIRST_AUTOSAVE_TIME    DATETIME     DEFAULT NULL COMMENT '首次自动保存时间',
    FERRY_SOURCE           VARCHAR(100) DEFAULT NULL COMMENT '摆渡来源',
    COMPLAINT_MEMBER_ID    BIGINT       DEFAULT NULL COMMENT '匿名投诉人ID',
    ATTACHMENTS            TEXT         DEFAULT NULL COMMENT '附件名冗余字段',

    PRIMARY KEY (ID),
    INDEX idx_sid (START_MEMBER_ID),
    INDEX idx_state (STATE),
    INDEX idx_cdate (CREATE_DATE)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内部邮件主表';


-- ============================================================
-- 2. 邮件事务表（对标 OA PRO_INMAIL_AFFAIR + ALTER）
-- ============================================================
CREATE TABLE pro_inmail_affair (
    ID                BIGINT       NOT NULL COMMENT '编号',
    MEMBER_ID         BIGINT       DEFAULT NULL COMMENT '收件者id',
    SENDER_ID         BIGINT       DEFAULT NULL COMMENT '发送者id',
    SUBJECT           VARCHAR(500) DEFAULT NULL COMMENT '标题',
    OBJECT_ID         BIGINT       DEFAULT NULL COMMENT '邮件主表id',
    STATE             INT          DEFAULT NULL COMMENT '状态: 0=sent 1=draft 2=inbox 3=deleted 5=secret',
    IS_DELETE         INT          DEFAULT NULL COMMENT '是否彻底删除',
    CREATE_DATE       DATETIME     DEFAULT NULL COMMENT '创建时间',
    UPDATE_DATE       DATETIME     DEFAULT NULL COMMENT '更新时间',
    FORWARD_MEMBER    VARCHAR(255) DEFAULT NULL COMMENT '转发人名称',
    ORG_DEPARTMENT_ID BIGINT       DEFAULT NULL COMMENT '部门id',
    ORG_ACCOUNT_ID    BIGINT       DEFAULT NULL COMMENT '单位id',
    READ_FLAG         INT          DEFAULT NULL COMMENT '是否已读',
    ATTACHMENTS_FLAG  INT          DEFAULT NULL COMMENT '是否有附件',
    AFFAIR_SIZE       BIGINT       DEFAULT NULL COMMENT '邮件大小',
    IS_FORWARD        INT          DEFAULT NULL COMMENT '是否转发了',
    IS_REPLY          INT          DEFAULT NULL COMMENT '是否已回复',
    REPLY_TYPE        INT          DEFAULT NULL COMMENT '回复类型',
    BROWSE_TIME       DATETIME     DEFAULT NULL COMMENT '浏览时间',
    DELETE_STATE      INT          DEFAULT NULL COMMENT '删除时状态(用于恢复)',
    IS_HANDLED        INT          DEFAULT NULL COMMENT '是否手动置为已办理',

    -- ALTER 补充字段
    PASS_THE_AUDIT    INT          DEFAULT 0 COMMENT '涉密审核: 0=审核中 1=通过',
    COLLECT           INT          DEFAULT 0 COMMENT '是否收藏',
    PATH              VARCHAR(500) DEFAULT NULL COMMENT '自定义文件夹路径',

    PRIMARY KEY (ID),
    INDEX idx_object_id (OBJECT_ID),
    INDEX idx_member (MEMBER_ID),
    INDEX idx_sender (SENDER_ID),
    INDEX idx_state (STATE),
    INDEX idx_read (READ_FLAG),
    INDEX idx_collect (COLLECT),
    INDEX idx_path (PATH)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内部邮件事务表';


-- ============================================================
-- 3. 加密密码箱（对标 OA PRO_INMAIL_SECRET）
-- ============================================================
CREATE TABLE pro_inmail_secret (
    ID          BIGINT       NOT NULL,
    MEMBER_ID   BIGINT       DEFAULT NULL,
    LOGIN_NAME  VARCHAR(100) DEFAULT NULL,
    PWD         VARCHAR(500) DEFAULT NULL,
    STATE       INT          DEFAULT NULL,
    CREATE_DATE DATETIME     DEFAULT NULL,
    UPDATE_DATE DATETIME     DEFAULT NULL,
    EXT1        VARCHAR(100) DEFAULT NULL,
    EXT2        VARCHAR(100) DEFAULT NULL,
    EXT3        VARCHAR(100) DEFAULT NULL,
    EXT4        VARCHAR(100) DEFAULT NULL,
    EXT5        VARCHAR(100) DEFAULT NULL,
    PRIMARY KEY (ID),
    INDEX idx_secret_member (MEMBER_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件加密密码箱';


-- ============================================================
-- 4. 邮件发送日志（对标 OA PRO_INMAIL_LOG）
-- ============================================================
CREATE TABLE pro_inmail_log (
    ID               BIGINT       NOT NULL,
    OBJECT_ID        BIGINT       DEFAULT NULL COMMENT '邮件id',
    START_UNIT_IP    VARCHAR(100) DEFAULT NULL,
    SEND_TO_UNIT_IDS TEXT         DEFAULT NULL,
    COPY_TO_UNIT_IDS TEXT         DEFAULT NULL,
    ATTACHMENT_NAME  LONGTEXT     DEFAULT NULL,
    ATTACHMENT_SIZE  VARCHAR(100) DEFAULT NULL,
    SECRET_ID        VARCHAR(100) DEFAULT NULL,
    IS_FILE          INT          DEFAULT 0,
    IS_LOG           INT          DEFAULT 0,
    result           VARCHAR(100) DEFAULT NULL,
    PRIMARY KEY (ID),
    INDEX idx_log_obj (OBJECT_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件发送日志';


-- ============================================================
-- 5. 自定义文件夹表（对标 OA PRO_INMAIL_FOLDER）
-- ============================================================
CREATE TABLE pro_inmail_folder (
    ID          BIGINT       NOT NULL,
    PATH        VARCHAR(500) NOT NULL,
    TYPE        VARCHAR(100) NOT NULL,
    MEMBER_ID   BIGINT       NOT NULL,
    IS_DELETE   INT          DEFAULT 0,
    FILE_NAME   VARCHAR(255) NOT NULL,
    RULE        TEXT         DEFAULT NULL,
    CREATE_DATE DATETIME     DEFAULT NULL,
    PRIMARY KEY (ID),
    INDEX idx_folder_member (MEMBER_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自定义文件夹';


-- ============================================================
-- 6. 群组/部门成员展开表（对标 OA PRO_INMAIL_SUMMARY_MEMBERS）
-- ============================================================
CREATE TABLE pro_inmail_summary_members (
    ID              BIGINT       NOT NULL,
    MAIL_SUMMARY_ID BIGINT       NOT NULL,
    MEMBER_ID       BIGINT       DEFAULT NULL,
    MEMBER_NAME     VARCHAR(200) DEFAULT NULL,
    MEMBER_LOGIN_NAME VARCHAR(100) DEFAULT NULL,
    TYPE            VARCHAR(50)  DEFAULT NULL COMMENT 'Member/Department/Account/Team',
    TYPE_ID         BIGINT       DEFAULT NULL,
    DETAIL          TEXT         DEFAULT NULL,
    ACCOUNT_ID      BIGINT       DEFAULT NULL,
    SORT            INT          DEFAULT 0,
    PRIMARY KEY (ID),
    INDEX idx_esm_sid (MAIL_SUMMARY_ID),
    INDEX idx_esm_mid (MEMBER_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组部门成员展开';


-- ============================================================
-- 7. 跨网邮件（对标 OA FORM_EMAIL_IS_ACROS_NETWORK）
-- ============================================================
CREATE TABLE form_email_is_acros_network (
    ID                BIGINT       NOT NULL,
    SOURCE_TARGET_ID  BIGINT       DEFAULT NULL,
    IS_ACROSS_NETWORK INT          DEFAULT 0,
    DB_TB             VARCHAR(100) DEFAULT NULL,
    CREATE_TIME       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='跨网邮件追踪';
