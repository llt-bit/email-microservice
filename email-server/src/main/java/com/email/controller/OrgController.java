package com.email.controller;

import com.email.common.R;
import com.email.platform.DBAgent;
import com.email.platform.entity.OrgMember;
import com.email.platform.entity.OrgUnit;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** 组织架构查询 API */
@RestController
@RequestMapping("/org")
public class OrgController {

    @PostMapping("/searchMember")
    public R<List<Map<String, Object>>> searchMember(@RequestBody Map<String, String> params) {
        String keyword = params.getOrDefault("keyword", "");
        if (keyword.isEmpty()) return R.ok(Collections.emptyList());
        Map<String, Object> p = new HashMap<>(); p.put("kw", "%" + keyword + "%");
        List<?> list = DBAgent.find("FROM OrgMember WHERE (name LIKE :kw OR loginName LIKE :kw) AND enabled=1", p);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object o : list) {
            OrgMember m = (OrgMember) o;
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id", m.getId()); r.put("name", m.getName()); r.put("loginName", m.getLoginName());
            r.put("departmentId", m.getDepartmentId()); r.put("accountId", m.getAccountId()); r.put("type", "Member");
            result.add(r);
        }
        return R.ok(result);
    }

    @GetMapping("/member/{id}") public R<OrgMember> member(@PathVariable Long id) { return R.ok(DBAgent.get(OrgMember.class, id)); }

    @GetMapping("/department/members/{id}")
    public R<List<Map<String, Object>>> deptMembers(@PathVariable Long id) {
        Map<String, Object> p = new HashMap<>(); p.put("did", id);
        List<?> list = DBAgent.find("FROM OrgMember WHERE departmentId=:did AND enabled=1", p);
        List<Map<String, Object>> r = new ArrayList<>();
        for (Object o : list) { OrgMember m = (OrgMember) o; Map<String, Object> mm = new LinkedHashMap<>();
            mm.put("id", m.getId()); mm.put("name", m.getName()); mm.put("loginName", m.getLoginName()); mm.put("type", "Member"); r.add(mm); }
        return R.ok(r);
    }

    @GetMapping("/departments")
    public R<List<OrgUnit>> depts(@RequestParam(required = false) Long accountId) {
        return R.ok(DBAgent.find("FROM OrgUnit", new HashMap<>()));
    }

    @PostMapping("/groupMembers")
    public R<List<Map<String, Object>>> groupMembers(@RequestBody Map<String, Object> params) {
        String entityId = (String) params.get("entityId");
        if (entityId == null) return R.ok(Collections.emptyList());
        String[] parts = entityId.split("\\|");
        if (parts.length < 2) return R.ok(Collections.emptyList());
        Long id = Long.parseLong(parts[1]);
        Map<String, Object> p = new HashMap<>(); p.put("did", id);
        List<?> list = DBAgent.find("FROM OrgMember WHERE departmentId=:did AND enabled=1", p);
        List<Map<String, Object>> r = new ArrayList<>();
        for (Object o : list) { OrgMember m = (OrgMember) o; Map<String, Object> mm = new LinkedHashMap<>();
            mm.put("id", m.getId().toString()); mm.put("name", m.getName()); mm.put("loginName", m.getLoginName()); mm.put("type", "Member"); r.add(mm); }
        return R.ok(r);
    }
}
