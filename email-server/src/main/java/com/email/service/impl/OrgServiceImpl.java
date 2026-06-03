package com.email.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.email.entity.*;
import com.email.mapper.*;
import com.email.service.OrgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 组织架构服务实现。
 */
@Slf4j
@Service
public class OrgServiceImpl implements OrgService {

    @Resource private OrgMemberMapper orgMemberMapper;
    @Resource private OrgDepartmentMapper orgDepartmentMapper;
    @Resource private OrgAccountMapper orgAccountMapper;
    @Resource private OrgTeamMapper orgTeamMapper;
    @Resource private OrgTeamMemberMapper orgTeamMemberMapper;

    /** OA 数据源（只读），配置在 application.yml */
    @Resource(name = "oaDataSource")
    private DataSource oaDataSource;

    @Override
    public List<OrgMember> searchMembers(String keyword, int limit) {
        return orgMemberMapper.searchByName(keyword).stream()
                .limit(limit > 0 ? limit : 50)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Cacheable(value = "orgMember", key = "#id")
    public OrgMember getMemberById(Long id) {
        return orgMemberMapper.selectById(id);
    }

    @Override
    public OrgMember getMemberByLoginName(String loginName) {
        return orgMemberMapper.findByLoginName(loginName);
    }

    @Override
    public List<OrgMember> getMembersByDepartmentId(Long departmentId) {
        return orgMemberMapper.findByDepartmentId(departmentId);
    }

    @Override
    public List<OrgMember> getMembersByAccountId(Long accountId) {
        return orgMemberMapper.findByAccountId(accountId);
    }

    @Override
    public List<OrgDepartment> getDepartments(Long accountId) {
        LambdaQueryWrapper<OrgDepartment> w = new LambdaQueryWrapper<>();
        if (accountId != null) w.eq(OrgDepartment::getAccountId, accountId);
        w.eq(OrgDepartment::getIsDeleted, 0);
        w.orderByAsc(OrgDepartment::getSort);
        return orgDepartmentMapper.selectList(w);
    }

    @Override
    public Map<String, Object> getEntityDetail(String type, Long id) {
        Map<String, Object> detail = new LinkedHashMap<>();
        switch (type) {
            case "Member":
                OrgMember m = orgMemberMapper.selectById(id);
                if (m != null) {
                    detail.put("id", m.getId());
                    detail.put("name", m.getName());
                    detail.put("loginName", m.getLoginName());
                    detail.put("departmentId", m.getDepartmentId());
                    detail.put("accountId", m.getAccountId());
                }
                break;
            case "Department":
                OrgDepartment d = orgDepartmentMapper.selectById(id);
                if (d != null) {
                    detail.put("id", d.getId());
                    detail.put("name", d.getName());
                    detail.put("fullName", d.getFullName());
                    detail.put("accountId", d.getAccountId());
                }
                break;
            case "Account":
                OrgAccount a = orgAccountMapper.selectById(id);
                if (a != null) {
                    detail.put("id", a.getId());
                    detail.put("name", a.getName());
                    detail.put("code", a.getCode());
                }
                break;
            case "Team":
                OrgTeam t = orgTeamMapper.selectById(id);
                if (t != null) {
                    detail.put("id", t.getId());
                    detail.put("name", t.getName());
                }
                break;
        }
        return detail;
    }

    // ==================== 同步任务 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fullSync() {
        log.info("开始全量同步组织架构...");
        long start = System.currentTimeMillis();

        if (oaDataSource == null) {
            log.warn("OA 数据源未配置，跳过组织架构同步");
            return;
        }

        try (Connection conn = oaDataSource.getConnection()) {
            // 同步单位
            syncAccounts(conn);
            // 同步部门
            syncDepartments(conn);
            // 同步人员
            syncMembers(conn);
            // 同步团队
            syncTeams(conn);

        } catch (Exception e) {
            log.error("全量同步组织架构失败", e);
        }

        log.info("全量同步完成，耗时 {}ms", System.currentTimeMillis() - start);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Scheduled(cron = "${email.org-sync.cron:0 */5 * * * ?}")
    public void incrementalSync() {
        String lastSync = getLastSyncTime();
        log.debug("增量同步组织架构，上次同步时间: {}", lastSync);

        if (oaDataSource == null) return;

        try (Connection conn = oaDataSource.getConnection()) {
            // 增量同步人员（金仓/MySQL/Oracle 写法不同，此处以 Oracle 为例）
            String sql = "SELECT ID, NAME, LOGIN_NAME, EMAIL, ORG_DEPARTMENT_ID, " +
                    "ORG_ACCOUNT_ID, ENABLED, IS_DELETED, SECRET_LEVEL_ID, UPDATE_DATE " +
                    "FROM V3X_ORG_MEMBER WHERE UPDATE_DATE > ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(lastSync));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        upsertMember(rs);
                    }
                }
            }
            updateLastSyncTime();
        } catch (Exception e) {
            log.error("增量同步组织架构失败", e);
        }
    }

    // ==================== 同步内部方法 ====================

    private void syncAccounts(Connection conn) throws SQLException {
        String sql = "SELECT ID, NAME, SHORT_NAME, CODE, SORT FROM V3X_ORG_ACCOUNT WHERE IS_DELETED = 0";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                OrgAccount a = new OrgAccount();
                a.setId(rs.getLong("ID"));
                a.setName(rs.getString("NAME"));
                a.setShortName(rs.getString("SHORT_NAME"));
                a.setCode(rs.getString("CODE"));
                a.setSort(rs.getInt("SORT"));
                a.setIsDeleted(0);
                a.setUpdateTime(LocalDateTime.now());
                saveOrUpdateAccount(a);
            }
        }
    }

    private void syncDepartments(Connection conn) throws SQLException {
        String sql = "SELECT ID, NAME, FULL_NAME, PARENT_ID, ACCOUNT_ID, SORT " +
                "FROM V3X_ORG_DEPARTMENT WHERE IS_DELETED = 0";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                OrgDepartment d = new OrgDepartment();
                d.setId(rs.getLong("ID"));
                d.setName(rs.getString("NAME"));
                d.setFullName(rs.getString("FULL_NAME"));
                d.setParentId(rs.getLong("PARENT_ID"));
                d.setAccountId(rs.getLong("ACCOUNT_ID"));
                d.setSort(rs.getInt("SORT"));
                d.setIsDeleted(0);
                d.setUpdateTime(LocalDateTime.now());
                saveOrUpdateDept(d);
            }
        }
    }

    private void syncMembers(Connection conn) throws SQLException {
        String sql = "SELECT ID, NAME, LOGIN_NAME, EMAIL, ORG_DEPARTMENT_ID, " +
                "ORG_ACCOUNT_ID, ENABLED, IS_DELETED, SECRET_LEVEL_ID " +
                "FROM V3X_ORG_MEMBER WHERE IS_DELETED = 0";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                upsertMember(rs);
            }
        }
    }

    private void syncTeams(Connection conn) throws SQLException {
        // 同步组
        String sql = "SELECT ID, NAME, ACCOUNT_ID FROM V3X_ORG_TEAM WHERE IS_DELETED = 0";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                OrgTeam t = new OrgTeam();
                t.setId(rs.getLong("ID"));
                t.setName(rs.getString("NAME"));
                t.setAccountId(rs.getLong("ACCOUNT_ID"));
                t.setIsDeleted(0);
                t.setUpdateTime(LocalDateTime.now());
                if (orgTeamMapper.selectById(t.getId()) != null) {
                    orgTeamMapper.updateById(t);
                } else {
                    orgTeamMapper.insert(t);
                }
            }
        }
        // 同步组成员关系
        sql = "SELECT TEAM_ID, MEMBER_ID FROM V3X_ORG_TEAM_MEMBER";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                OrgTeamMember tm = new OrgTeamMember();
                tm.setTeamId(rs.getLong("TEAM_ID"));
                tm.setMemberId(rs.getLong("MEMBER_ID"));
                // UPSERT: 先删再插（组关系通常不大）
                LambdaQueryWrapper<OrgTeamMember> w = new LambdaQueryWrapper<>();
                w.eq(OrgTeamMember::getTeamId, tm.getTeamId())
                 .eq(OrgTeamMember::getMemberId, tm.getMemberId());
                if (orgTeamMemberMapper.selectCount(w) == 0) {
                    orgTeamMemberMapper.insert(tm);
                }
            }
        }
    }

    private void upsertMember(ResultSet rs) throws SQLException {
        OrgMember m = new OrgMember();
        m.setId(rs.getLong("ID"));
        m.setName(rs.getString("NAME"));
        m.setLoginName(rs.getString("LOGIN_NAME"));
        m.setEmail(rs.getString("EMAIL"));
        m.setDepartmentId(rs.getLong("ORG_DEPARTMENT_ID"));
        m.setAccountId(rs.getLong("ORG_ACCOUNT_ID"));
        m.setEnabled(rs.getInt("ENABLED"));
        m.setIsDeleted(rs.getInt("IS_DELETED"));
        m.setSecretLevelId(rs.getLong("SECRET_LEVEL_ID"));
        m.setUpdateTime(LocalDateTime.now());
        saveOrUpdateMember(m);
    }

    private void saveOrUpdateAccount(OrgAccount a) {
        OrgAccount exist = orgAccountMapper.selectById(a.getId());
        if (exist != null) orgAccountMapper.updateById(a);
        else orgAccountMapper.insert(a);
    }

    private void saveOrUpdateDept(OrgDepartment d) {
        OrgDepartment exist = orgDepartmentMapper.selectById(d.getId());
        if (exist != null) orgDepartmentMapper.updateById(d);
        else orgDepartmentMapper.insert(d);
    }

    private void saveOrUpdateMember(OrgMember m) {
        OrgMember exist = orgMemberMapper.selectById(m.getId());
        if (exist != null) orgMemberMapper.updateById(m);
        else orgMemberMapper.insert(m);
    }

    private String getLastSyncTime() {
        // 简化实现：内存中的时间戳
        return "2024-01-01 00:00:00";
    }

    private void updateLastSyncTime() {
        // 简化实现：更新内存中的时间戳
    }
}
