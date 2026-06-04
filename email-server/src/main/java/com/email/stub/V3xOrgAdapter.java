package com.email.stub;

import com.email.platform.DBAgent;
import com.email.platform.Strings;
import com.email.platform.entity.OrgMember;
import com.email.platform.entity.OrgUnit;

import java.util.*;

/**
 * OA 组织架构类型适配 —— 让 OA 的 V3xOrgMember/V3xOrgDepartment/V3xOrgEntity/V3xOrgAccount 代码可直接复用。
 * 这些类直接包装我们自己的 OrgMember/OrgUnit 实体。
 */
public class V3xOrgAdapter {

    /** 实体类型接口（OA V3xOrgEntity） */
    public interface Entity { Long getId(); String getName(); String getEntityType(); }

    /** 人员（OA V3xOrgMember） */
    public static class Member implements Entity {
        private Long id; private String name; private String loginName;
        private Long orgDepartmentId; private Long orgAccountId; private String emailAddress;
        public Member() {}
        public Member(OrgMember m) { if (m != null) { id = m.getId(); name = m.getName(); loginName = m.getLoginName(); orgDepartmentId = m.getDepartmentId(); orgAccountId = m.getAccountId(); } }
        public Long getId() { return id; } public String getName() { return name; } public String getLoginName() { return loginName; }
        public Long getOrgDepartmentId() { return orgDepartmentId; } public Long getOrgAccountId() { return orgAccountId; }
        public String getEntityType() { return "Member"; }
        public String getEmailAddress() { return emailAddress; }
        public void setId(Long v) { this.id = v; } public void setName(String v) { this.name = v; }
        public void setLoginName(String v) { this.loginName = v; }
        public void setOrgDepartmentId(Long v) { this.orgDepartmentId = v; }
        public void setOrgAccountId(Long v) { this.orgAccountId = v; }
        public void setEmailAddress(String v) { this.emailAddress = v; }
    }

    /** 部门（OA V3xOrgDepartment） */
    public static class Department implements Entity {
        private Long id; private String name;
        public Department() {} public Department(OrgUnit d) { if (d != null) { id = d.getId(); name = d.getName(); } }
        public Long getId() { return id; } public String getName() { return name; } public String getEntityType() { return "Department"; }
    }

    /** 单位（OA V3xOrgAccount） */
    public static class Account implements Entity {
        private Long id; private String name;
        public Long getId() { return id; } public String getName() { return name; } public String getEntityType() { return "Account"; }
        public void setId(Long v) { this.id = v; } public void setName(String v) { this.name = v; }
    }

    /** 从 OrgMember 创建 V3x Member */
    public static Member toMember(Long id) { OrgMember m = DBAgent.get(OrgMember.class, id); return m != null ? new Member(m) : null; }

    /** 获取实体详情（对齐 OA orgManager.getEntity("Member|123")） */
    public static Entity getEntity(String fullId) {
        if (Strings.isBlank(fullId)) return null;
        String[] p = fullId.split("\\|");
        if (p.length < 2) return null;
        switch (p[0]) {
            case "Member": return toMember(Long.parseLong(p[1]));
            case "Department": OrgUnit d = DBAgent.get(OrgUnit.class, Long.parseLong(p[1])); return d != null ? new Department(d) : null;
            default: return null;
        }
    }

    /** 获取实体列表 */
    public static List<Entity> getEntities(String ids) {
        List<Entity> list = new ArrayList<>();
        if (Strings.isBlank(ids)) return list;
        for (String s : ids.split(",")) { Entity e = getEntity(s.trim()); if (e != null) list.add(e); }
        return list;
    }

    /** 解析收件人 */
    public static Set<Member> getMembersByTypeAndIds(String ids) {
        Set<Member> set = new LinkedHashSet<>();
        if (Strings.isBlank(ids)) return set;
        final Set<Long> seen = new HashSet<>();
        for (String s : ids.split(",")) {
            String[] p = s.trim().split("\\|");
            if (p.length >= 2 && "Member".equals(p[0])) {
                Long id = Long.parseLong(p[1]);
                if (seen.add(id)) { Member m = toMember(id); if (m != null) set.add(m); }
            }
        }
        return set;
    }
}
