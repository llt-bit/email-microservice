package com.email.service;

import com.email.entity.OrgDepartment;
import com.email.entity.OrgMember;

import java.util.List;
import java.util.Map;

/**
 * 组织架构服务 —— 本地组织表查询 + 从 OA 定时同步。
 */
public interface OrgService {

    /** 按关键词搜索人员 */
    List<OrgMember> searchMembers(String keyword, int limit);

    /** 按 ID 查人员 */
    OrgMember getMemberById(Long id);

    /** 按登录名查人员 */
    OrgMember getMemberByLoginName(String loginName);

    /** 按部门ID查人员清单 */
    List<OrgMember> getMembersByDepartmentId(Long departmentId);

    /** 按单位ID查人员清单 */
    List<OrgMember> getMembersByAccountId(Long accountId);

    /** 查询部门列表（按单位过滤） */
    List<OrgDepartment> getDepartments(Long accountId);

    /** 查询部门/单位/组详情（统一接口，前端选人组件用） */
    Map<String, Object> getEntityDetail(String type, Long id);

    /** 手动触发组织架构全量同步 */
    void fullSync();

    /** 增量同步（由定时任务调用） */
    void incrementalSync();
}
