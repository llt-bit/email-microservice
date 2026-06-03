-- ============================================================
-- 组织架构本地表（从 OA Oracle 数据库同步到邮件 MySQL 库）
-- 邮件独立运行需要本地缓存组织架构，OA 宕机时仍可查询
-- ============================================================

-- 单位表
CREATE TABLE org_account (
    id          BIGINT        NOT NULL COMMENT '单位ID（与OA一致）',
    name        VARCHAR(200)  NOT NULL COMMENT '单位名称',
    short_name  VARCHAR(100)  DEFAULT NULL COMMENT '简称',
    code        VARCHAR(100)  DEFAULT NULL COMMENT '单位编码',
    sort        INT           DEFAULT 0 COMMENT '排序',
    is_deleted  TINYINT       DEFAULT 0 COMMENT '是否删除',
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单位表(同步自OA)';

-- 部门表
CREATE TABLE org_department (
    id          BIGINT        NOT NULL COMMENT '部门ID（与OA一致）',
    name        VARCHAR(200)  NOT NULL COMMENT '部门名称',
    full_name   VARCHAR(500)  DEFAULT NULL COMMENT '部门全路径名称',
    parent_id   BIGINT        DEFAULT NULL COMMENT '上级部门ID',
    account_id  BIGINT        NOT NULL COMMENT '所属单位ID',
    sort        INT           DEFAULT 0 COMMENT '排序',
    is_deleted  TINYINT       DEFAULT 0 COMMENT '是否删除',
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (id),
    INDEX idx_od_acct (account_id),
    INDEX idx_od_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表(同步自OA)';

-- 人员表
CREATE TABLE org_member (
    id              BIGINT        NOT NULL COMMENT '人员ID（与OA一致）',
    name            VARCHAR(200)  NOT NULL COMMENT '姓名',
    login_name      VARCHAR(100)  NOT NULL COMMENT '登录名(唯一)',
    email           VARCHAR(200)  DEFAULT NULL COMMENT '邮箱地址',
    department_id   BIGINT        DEFAULT NULL COMMENT '所属部门ID',
    account_id      BIGINT        DEFAULT NULL COMMENT '所属单位ID',
    enabled         TINYINT       DEFAULT 1 COMMENT '是否启用',
    is_deleted      TINYINT       DEFAULT 0 COMMENT '是否删除',
    secret_level_id BIGINT        DEFAULT NULL COMMENT '人员密级ID',
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_om_login (login_name),
    INDEX idx_om_dept (department_id),
    INDEX idx_om_acct (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员表(同步自OA)';

-- 组/团队表
CREATE TABLE org_team (
    id          BIGINT        NOT NULL COMMENT '组ID（与OA一致）',
    name        VARCHAR(200)  NOT NULL COMMENT '组名称',
    account_id  BIGINT        DEFAULT NULL COMMENT '所属单位ID',
    is_deleted  TINYINT       DEFAULT 0 COMMENT '是否删除',
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组/团队表(同步自OA)';

-- 组成员关系表
CREATE TABLE org_team_member (
    team_id     BIGINT NOT NULL COMMENT '组ID',
    member_id   BIGINT NOT NULL COMMENT '成员ID',
    PRIMARY KEY (team_id, member_id),
    INDEX idx_otm_member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组成员关系(同步自OA)';
