package com.email.service;

import com.email.entity.InMailSummaryMembersPo;
import com.email.platform.DBAgent;
import com.email.platform.Strings;
import com.email.platform.entity.OrgMember;
import com.email.platform.entity.OrgUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 组织架构服务 —— 替代 OA 的 OrgManager + OrgHelper。
 *
 * <p>内部邮件代码中所有 orgManager.xxx() 和 OrgHelper.xxx() 调用都通过本服务实现。
 * 数据来源：本地 org_member / org_department / org_account 表（定期从 OA 同步）。
 *
 * <p>注意：OA 代码用的实体类型是 V3xOrgMember / V3xOrgDepartment / V3xOrgAccount，
 * 但在适配层中我们统一用平台实体（OrgMember / OrgUnit）。由于 HQL 中用的是 OrgMember/OrgUnit
 * 的类名，hbm.xml 也需要相应映射。这里简化处理——getMemberById 等直接查表返回 Map。
 * 真正的 V3xOrg* 类在后续阶段4搬迁时再写兼容包装。</p>
 */
@Slf4j
@Service("orgManager")  // Bean 名对齐 OA，使 AppContext.getBean("orgManager") 可用
public class OrgService {

    // ==================== 人员查询 ====================

    public OrgMember getMemberById(Long id) {
        if (id == null) return null;
        return DBAgent.get(OrgMember.class, id);
    }

    public OrgUnit getDepartmentById(Long id) {
        if (id == null) return null;
        return DBAgent.get(OrgUnit.class, id);
    }

    /**
     * 核心方法：解析 "Member|1,Department|2,Account|3,Team|4" 格式的 ID 列表，
     * 将群组/部门/单位展开为实际成员集合。对齐 OA orgManager.getMembersByTypeAndIds(ids)。
     */
    @SuppressWarnings("unchecked")
    public Set<OrgMember> getMembersByTypeAndIds(String ids) {
        Set<OrgMember> result = new LinkedHashSet<>();
        if (Strings.isBlank(ids)) return result;

        Map<Long, OrgMember> seen = new HashMap<>();

        for (String idStr : ids.split(",")) {
            idStr = idStr.trim();
            if (idStr.isEmpty()) continue;

            String[] parts = idStr.split("\\|");
            String type = parts.length >= 2 ? parts[0] : "Member";
            String idVal = parts.length >= 2 ? parts[1] : parts[0];

            try {
                Long entityId = Long.parseLong(idVal);
                List<OrgMember> members = resolveMembers(type, entityId);
                for (OrgMember m : members) {
                    if (!seen.containsKey(m.getId())) {
                        seen.put(m.getId(), m);
                        result.add(m);
                    }
                }
            } catch (NumberFormatException e) {
                log.debug("无法解析组织ID: {}", idStr);
            }
        }
        return result;
    }

    private List<OrgMember> resolveMembers(String type, Long entityId) {
        Map<String, Object> params = new HashMap<>();
        switch (type) {
            case "Member":
                OrgMember m = DBAgent.get(OrgMember.class, entityId);
                return m != null ? Collections.singletonList(m) : Collections.emptyList();

            case "Department": {
                params.put("deptId", entityId);
                return DBAgent.find("FROM OrgMember WHERE departmentId=:deptId AND enabled=1", params);
            }
            case "Account": {
                params.put("acctId", entityId);
                return DBAgent.find("FROM OrgMember WHERE accountId=:acctId AND enabled=1", params);
            }
            case "Team": {
                // 从 org_team_member 表查团队成员
                params.put("teamId", entityId);
                List<?> rels = DBAgent.find(
                        "SELECT tm.memberId FROM OrgTeamMember tm WHERE tm.teamId=:teamId", params);
                List<Long> memberIds = new ArrayList<>();
                for (Object row : rels) {
                    if (row instanceof Object[]) {
                        memberIds.add((Long) ((Object[]) row)[0]);
                    } else {
                        memberIds.add((Long) row);
                    }
                }
                if (memberIds.isEmpty()) return Collections.emptyList();
                // batch get
                List<OrgMember> list = new ArrayList<>();
                for (Long mid : memberIds) {
                    OrgMember om = DBAgent.get(OrgMember.class, mid);
                    if (om != null) list.add(om);
                }
                return list;
            }
            default:
                return Collections.emptyList();
        }
    }

    // ==================== 实体查询（OA V3xOrgEntity 兼容） ====================

    /** 对齐 OA orgManager.getEntity("Member|123") */
    public Map<String, Object> getEntity(String fullId) {
        if (Strings.isBlank(fullId)) return null;
        String[] parts = fullId.split("\\|");
        if (parts.length < 2) return null;
        String type = parts[0];
        Long id = Long.parseLong(parts[1]);

        Map<String, Object> entity = new LinkedHashMap<>();
        switch (type) {
            case "Member": {
                OrgMember m = getMemberById(id);
                if (m != null) {
                    entity.put("id", m.getId()); entity.put("name", m.getName());
                    entity.put("type", "Member");
                }
                break;
            }
            case "Department": {
                OrgUnit d = getDepartmentById(id);
                if (d != null) {
                    entity.put("id", d.getId()); entity.put("name", d.getName());
                    entity.put("type", "Department");
                }
                break;
            }
            case "Account": {
                entity.put("id", id); entity.put("type", "Account");
                break;
            }
        }
        return entity.isEmpty() ? null : entity;
    }

    /** 对齐 OA orgManager.getEntities(ids) */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getEntities(String ids) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (Strings.isBlank(ids)) return list;

        for (String idStr : ids.split(",")) {
            idStr = idStr.trim();
            if (idStr.isEmpty()) continue;
            Map<String, Object> e = getEntity(idStr);
            if (e != null) list.add(e);
        }
        return list;
    }

    // ==================== 部门/单位 ====================

    public List<OrgMember> getMembersByDepartmentId(Long deptId) {
        Map<String, Object> params = new HashMap<>();
        params.put("deptId", deptId);
        return DBAgent.find("FROM OrgMember WHERE departmentId=:deptId AND enabled=1", params);
    }

    public List<OrgMember> getAllMembers(Long accountId) {
        Map<String, Object> params = new HashMap<>();
        params.put("acctId", accountId);
        return DBAgent.find("FROM OrgMember WHERE accountId=:acctId AND enabled=1", params);
    }

    // ==================== OrgHelper 兼容静态方法 ====================

    /** 静态包装——邮件代码直接调用 OrgHelper.getMember(id) */
    private static OrgService _instance;
    public static void setInstance(OrgService s) { _instance = s; }

    public static OrgMember getMember(Long id) {
        return _instance != null ? _instance.getMemberById(id) : null;
    }

    public static OrgUnit getDepartment(Long id) {
        return _instance != null ? _instance.getDepartmentById(id) : null;
    }
}
