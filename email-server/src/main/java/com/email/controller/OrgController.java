package com.email.controller;

import com.email.common.R;
import com.email.entity.OrgDepartment;
import com.email.entity.OrgMember;
import com.email.service.OrgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * 组织架构查询 API —— 写邮件选人组件使用。
 */
@Slf4j
@RestController
@RequestMapping("/org")
public class OrgController {

    @Resource private OrgService orgService;

    @PostMapping("/searchMember")
    public R<List<Map<String, Object>>> searchMember(@RequestBody Map<String, String> params) {
        String keyword = params.getOrDefault("keyword", "");
        int limit = Integer.parseInt(params.getOrDefault("limit", "50"));
        List<OrgMember> members = orgService.searchMembers(keyword, limit);

        List<Map<String, Object>> list = new ArrayList<>();
        for (OrgMember m : members) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", m.getId());
            map.put("name", m.getName());
            map.put("loginName", m.getLoginName());
            map.put("departmentId", m.getDepartmentId());
            map.put("accountId", m.getAccountId());
            map.put("type", "Member");
            list.add(map);
        }
        return R.ok(list);
    }

    @GetMapping("/member/{id}")
    public R<OrgMember> getMember(@PathVariable Long id) {
        return R.ok(orgService.getMemberById(id));
    }

    @GetMapping("/department/members/{departmentId}")
    public R<List<Map<String, Object>>> getDepartmentMembers(@PathVariable Long departmentId) {
        List<OrgMember> members = orgService.getMembersByDepartmentId(departmentId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (OrgMember m : members) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", m.getId());
            map.put("name", m.getName());
            map.put("loginName", m.getLoginName());
            map.put("departmentId", m.getDepartmentId());
            map.put("type", "Member");
            list.add(map);
        }
        return R.ok(list);
    }

    @GetMapping("/departments")
    public R<List<OrgDepartment>> getDepartments(@RequestParam(required = false) Long accountId) {
        return R.ok(orgService.getDepartments(accountId));
    }

    @GetMapping("/entity/{type}/{id}")
    public R<Map<String, Object>> getEntity(@PathVariable String type, @PathVariable Long id) {
        return R.ok(orgService.getEntityDetail(type, id));
    }

    @PostMapping("/groupMembers")
    public R<List<Map<String, Object>>> getGroupMembers(@RequestBody Map<String, Object> params) {
        String entityId = (String) params.get("entityId");
        if (entityId == null) return R.ok(Collections.emptyList());

        // entityId 格式: "Department|123" 或 "Account|456" 或 "Team|789"
        String[] parts = entityId.split("\\|");
        if (parts.length < 2) return R.ok(Collections.emptyList());

        String type = parts[0];
        Long id = Long.parseLong(parts[1]);
        List<OrgMember> members;

        switch (type) {
            case "Department":
                members = orgService.getMembersByDepartmentId(id); break;
            case "Account":
                members = orgService.getMembersByAccountId(id); break;
            default:
                members = Collections.emptyList();
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (OrgMember m : members) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", m.getId().toString());
            map.put("name", m.getName());
            map.put("loginName", m.getLoginName());
            map.put("type", "Member");
            list.add(map);
        }
        return R.ok(list);
    }
}
