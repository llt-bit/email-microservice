# 内部邮件剥离 - Hibernate 复用方案执行计划

> 本文档记录架构决策和详细执行步骤，供后续逐步执行。
> 核心原则：**邮件业务代码原样复用，只替换 OA 平台调用点**。

## 一、关键架构决策（Opus 4.8 评估确认）

### 决策1：改用 Hibernate（放弃 MyBatis）
OA 的 `InternalMailDaoImpl`(2321行) 和 `InternalMailManagerImpl`(2971行) 全部基于 Hibernate HQL，
包含多表 JOIN、动态排序、全文搜索、分页。改写成 MyBatis SQL 风险极高，违背"不重写"原则。
**改用 Hibernate 后，Manager/DAO 可近 100% 原样复制。**

### 决策2：复用 OA 原表名和列名（关键！）
新邮件库的表直接沿用 OA 表名/列名：
- `pro_inmail_summary`（不是 email_summary）
- `pro_inmail_affair`
- `pro_inmail_secret`
- `pro_inmail_folder`
- `pro_inmail_summary_members`
- `pro_inmail_log`
- 列名也保持 `SUBJECT`、`STATE`、`OBJECT_ID`、`START_MEMBER_ID` 等

**好处：**
1. OA 的 `*.hbm.xml` 原样复制，一字不改
2. 所有 HQL 原样复制
3. PO 类原样复制（不需要 @TableField）
4. 数据迁移时表结构一致，可直接 `INSERT...SELECT`

### 决策3：写一个同名 DBAgent 包装 Hibernate
创建 `com.email.platform.DBAgent`，提供与 OA 完全相同的静态方法签名：
- `find(hql, params)` / `find(hql, params, flipInfo)`
- `get(Class, id)` / `save()` / `update()` / `delete()` / `updateAll()` / `saveAll()`
- `findByNamedQuery(name, params)` / `count()` / `bulkUpdate()` / `deleteAll()`
底层用 Hibernate SessionFactory。这样 Manager/DAO 里的 `DBAgent.xxx()` 调用零改动。

### 决策4：FlipInfo 也要做一个兼容类
OA 的分页对象 `com.seeyon.ctp.util.FlipInfo`，需要做一个同名同方法的类，
让 DAO 里的 `fi.setPage()`、`fi.getData()`、`fi.setTotal()` 等零改动。

## 二、需要本地化的 OA 平台实体（HQL JOIN 用到）

HQL 中 JOIN 了这些 OA 实体，需要为它们建 hbm 映射到本地组织/附件/密级表：
| OA 实体 | HQL别名 | 用途 | 本地表 |
|---------|--------|------|--------|
| `OrgMember` | s | 按发件人/收件人姓名搜索、排序 | org_member |
| `OrgUnit` | ou | 按部门排序 | org_department/org_account |
| `Attachment` | a | 按附件名搜索 | 附件表 |
| `FileSecretMap` | fsm | 按密级排序 | 密级映射表 |
| `FileSecretLevel` | fsl | 密级等级 | 密级等级表 |

这些实体的 PO 和 hbm 也需要从 OA 复制简化版。

## 三、执行步骤（机械迁移，可用 DeepSeek 执行）

### 阶段1：基础设施（platform 包）
- [ ] 重写 SQL DDL：表名/列名沿用 OA（pro_inmail_*）
- [ ] 配置 Hibernate（SessionFactory + 事务管理 + 方言）
- [ ] 写 `DBAgent` 包装类（同 OA 签名）
- [ ] 写 `FlipInfo` 兼容类
- [ ] 写 `Strings`、`SQLWildcardUtil`、`UUIDLong`、`Datetimes` 等 OA 工具类的兼容实现
- [ ] 写 `BusinessException` 兼容类（已有）

### 阶段2：PO + hbm（原样复制）
- [ ] InMailSummary + hbm（去掉 @TableField，改回原列名）
- [ ] InMailAffair + hbm
- [ ] InMailSecret + hbm
- [ ] InMailFolder + hbm（OA无hbm，需补）
- [ ] InMailSummaryMembersPo + hbm
- [ ] MyQuery.hbm.xml（命名查询）
- [ ] 平台实体：OrgMember/OrgUnit/Attachment/FileSecretMap/FileSecretLevel + hbm

### 阶段3：OA 平台 API 适配层
- [ ] `OrgService`：替代 OrgManager（getMemberById/getMembersByTypeAndIds/getEntities...）
- [ ] `AttachmentManager` 适配：底层走 MinIO + 本地附件表
- [ ] `UserMessageManager` 适配：本地通知
- [ ] `FileSecretManager`/`FileSecretMapManager`/`CheckSecretManager` 适配
- [ ] `AppContext` 兼容类：getCurrentUser()→UserContext, getBean()→Spring, getSystemProperty()→配置

### 阶段4：邮件业务代码（原样复制，只改 import 和 OA 调用点）
- [ ] InMailUtil（已复制，需调整）
- [ ] EmailDESUtil（已复制）
- [ ] InternalMailDaoImpl（2321行，原样复制改 import）
- [ ] FolderMailDaoImpl
- [ ] InMailSummaryMembersDaoImpl
- [ ] InternalMailManagerImpl（2971行，原样复制改 import）
- [ ] FolderMailManagerImpl
- [ ] InMailCountManagerImpl
- [ ] InMailSummaryMembersManagerImpl
- [ ] NewEmailUtils（2589行，原样复制改 import）

### 阶段5：Controller（REST API）
- [ ] EmailController（从 EmailPCResource 迁移，JAX-RS→Spring MVC）
- [ ] 其余 Controller

### 阶段6：清理
- [ ] 删除之前的 MyBatis 简化版：EmailServiceImpl、各 Mapper、Entity（email_*）
- [ ] 删除 MybatisPlusConfig

## 四、OA → 独立 调用对照表（同主方案文档5.2节）

| OA 调用 | 替换为 |
|---------|--------|
| `AppContext.getCurrentUser()` | `AppContext.getCurrentUser()`（兼容类内部走 UserContext） |
| `AppContext.getBean("xxx")` | `AppContext.getBean("xxx")`（兼容类走 Spring） |
| `DBAgent.xxx()` | 不变（包装类） |
| `OrgManager` | `OrgService` |
| `AttachmentManager` | 适配类（MinIO） |
| `UserMessageManager` | 适配类（本地通知） |

## 五、当前进度

- [x] 项目骨架（需调整：去 MyBatis 加 Hibernate）
- [x] JWT 认证体系（保留）
- [x] UserContext / UserContextHolder（保留）
- [x] MinIO 工具（保留）
- [x] PO 类初版（需调整：改回 OA 原列名）
- [ ] 以上阶段1-6
