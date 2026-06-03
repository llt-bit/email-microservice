-- ============================================================
-- 组织架构本地表 - KingbaseES V8R6 版本
-- ============================================================

CREATE TABLE org_account (
    id          BIGINT        NOT NULL,
    name        VARCHAR(200)  NOT NULL,
    short_name  VARCHAR(100)  DEFAULT NULL,
    code        VARCHAR(100)  DEFAULT NULL,
    sort        INT           DEFAULT 0,
    is_deleted  SMALLINT      DEFAULT 0,
    update_time TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
COMMENT ON TABLE org_account IS '单位表(同步自OA)';

CREATE TABLE org_department (
    id          BIGINT        NOT NULL,
    name        VARCHAR(200)  NOT NULL,
    full_name   VARCHAR(500)  DEFAULT NULL,
    parent_id   BIGINT        DEFAULT NULL,
    account_id  BIGINT        NOT NULL,
    sort        INT           DEFAULT 0,
    is_deleted  SMALLINT      DEFAULT 0,
    update_time TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE INDEX idx_od_acct ON org_department (account_id);
CREATE INDEX idx_od_parent ON org_department (parent_id);
COMMENT ON TABLE org_department IS '部门表(同步自OA)';

CREATE TABLE org_member (
    id              BIGINT        NOT NULL,
    name            VARCHAR(200)  NOT NULL,
    login_name      VARCHAR(100)  NOT NULL,
    email           VARCHAR(200)  DEFAULT NULL,
    department_id   BIGINT        DEFAULT NULL,
    account_id      BIGINT        DEFAULT NULL,
    enabled         SMALLINT      DEFAULT 1,
    is_deleted      SMALLINT      DEFAULT 0,
    secret_level_id BIGINT        DEFAULT NULL,
    update_time     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uk_om_login ON org_member (login_name);
CREATE INDEX idx_om_dept ON org_member (department_id);
CREATE INDEX idx_om_acct ON org_member (account_id);
COMMENT ON TABLE org_member IS '人员表(同步自OA)';

CREATE TABLE org_team (
    id          BIGINT        NOT NULL,
    name        VARCHAR(200)  NOT NULL,
    account_id  BIGINT        DEFAULT NULL,
    is_deleted  SMALLINT      DEFAULT 0,
    update_time TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
COMMENT ON TABLE org_team IS '组/团队表(同步自OA)';

CREATE TABLE org_team_member (
    team_id     BIGINT NOT NULL,
    member_id   BIGINT NOT NULL,
    PRIMARY KEY (team_id, member_id)
);
CREATE INDEX idx_otm_member ON org_team_member (member_id);
COMMENT ON TABLE org_team_member IS '组成员关系(同步自OA)';
