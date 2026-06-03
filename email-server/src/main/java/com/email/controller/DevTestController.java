package com.email.controller;

import com.email.common.R;
import com.email.entity.InMailAffair;
import com.email.entity.InMailSummary;
import com.email.platform.DBAgent;
import com.email.platform.FlipInfo;
import com.email.platform.AppContext;
import com.email.platform.OrgHelper;
import com.email.platform.entity.OrgMember;
import com.email.security.UserInfo;
import com.email.service.OrgService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * 开发环境自测端点 —— 验证 DBAgent/FlipInfo/HQL/事务完整性。
 * 生产环境删除此类。
 */
@RestController
@RequestMapping("/devtest")
public class DevTestController {

    private int pass; private int fail;
    private void ok(String s) { pass++; }
    private void no(String s, Object v) {
        fail++;
        System.err.println("FAIL: " + s + " => " + (v != null ? v.toString().substring(0, Math.min(200, v.toString().length())) : "null"));
    }

    @Resource private OrgService orgService;

    @GetMapping("/run")
    public R<Map<String, Object>> runAll() {
        pass = 0; fail = 0;

        test1_FindAll();
        test2_FindByNamedQuery();
        test3_GetById();
        test4_FlipInfoPagination();
        test5_HQL_Join();
        test6_ChineseSearch();
        test7_CUD();
        test8_Count();
        test9_AppContextBean();
        test10_AppContextUser();
        test11_OrgServiceResolve();
        test12_OrgHelperStatic();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pass", pass);
        result.put("fail", fail);
        result.put("total", pass + fail);
        result.put("allPassed", fail == 0);
        return fail == 0 ? R.ok(result) : R.fail(500, fail + " / " + (pass + fail) + " 项失败");
    }

    // 1. DBAgent.find 基础查询
    void test1_FindAll() {
        Map<String, Object> params = new HashMap<>();
        params.put("mid", 1L);
        List<?> r = DBAgent.find("FROM InMailAffair WHERE memberId=:mid", params);
        if (r.size() >= 4) ok("1.find(带参HQL, " + r.size() + "条)");
        else no("1.find", r.size());
    }

    // 2. findByNamedQuery 命名查询
    void test2_FindByNamedQuery() {
        Map<String, Object> params = new HashMap<>();
        params.put("memberId", 1L);
        params.put("state", 2); // inbox
        List<?> r = DBAgent.findByNamedQuery("internalmail_hibernate_findAffairByMemberIdAndState", params);
        if (!r.isEmpty()) ok("2.namedQuery(" + r.size() + "条)");
        else no("2.namedQuery", "empty");
    }

    // 3. DBAgent.get 按ID查询
    void test3_GetById() {
        InMailAffair a = DBAgent.get(InMailAffair.class, 2001L);
        if (a != null && "测试邮件001".equals(a.getSubject())) ok("3.get(ID)");
        else no("3.get", a);
    }

    // 4. FlipInfo 分页
    void test4_FlipInfoPagination() {
        FlipInfo fi = new FlipInfo();
        fi.setPage(1);
        fi.setSize(2);
        fi.setNeedTotal(true);
        Map<String, Object> p = new HashMap<>();
        p.put("mid", 1L);
        DBAgent.find("FROM InMailAffair WHERE memberId=:mid", p, fi);

        if (fi.getTotal() >= 4 && fi.getData().size() <= 2) ok("4.FlipInfo(total=" + fi.getTotal() + ",pageSize=" + fi.getData().size() + ")");
        else no("4.FlipInfo", "total=" + fi.getTotal() + ",data=" + fi.getData().size());
    }

    // 5. HQL JOIN 查询
    void test5_HQL_Join() {
        Map<String, Object> params = new HashMap<>();
        params.put("mid", 1L);
        List<?> r = DBAgent.find(
            "SELECT a.subject, s.name FROM InMailAffair a, OrgMember s " +
            "WHERE a.senderId=s.id AND a.memberId=:mid", params);
        if (!r.isEmpty()) {
            Object[] row = (Object[]) r.get(0);
            if (row.length == 2) ok("5.HQL_JOIN(" + row[0] + "/" + row[1] + ")");
            else no("5.HQL_JOIN", r.get(0));
        } else no("5.HQL_JOIN", "empty");
    }

    // 6. 中文搜索
    void test6_ChineseSearch() {
        Map<String, Object> params = new HashMap<>();
        params.put("kw", "%重要%");
        List<?> r = DBAgent.find(
            "FROM InMailSummary WHERE SUBJECT LIKE :kw", params);
        if (r.size() == 1 && ((InMailSummary)r.get(0)).getSubject().contains("中文")) ok("6.中文搜索(" + ((InMailSummary)r.get(0)).getSubject() + ")");
        else no("6.中文搜索", r.size() + "条");
    }

    // 7. CUD (增删改)
    void test7_CUD() {
        // save
        InMailAffair a = new InMailAffair();
        a.setIdIfNew();
        Long newId = a.getId();
        a.setMemberId(1L);
        a.setSenderId(1L);
        a.setSubject("DBAgent自测插入");
        a.setObjectId(1001L);
        a.setState(2);
        a.setReadFlag(false);
        a.setAttachmentsFlag(false);
        a.setCreateDate(new java.util.Date());
        DBAgent.save(a);
        InMailAffair check = DBAgent.get(InMailAffair.class, newId);
        if (check != null && check.getSubject().equals("DBAgent自测插入")) ok("7a.save");
        else { no("7a.save", check); return; }

        // update
        check.setSubject("DBAgent自测更新");
        DBAgent.update(check);
        InMailAffair check2 = DBAgent.get(InMailAffair.class, newId);
        if ("DBAgent自测更新".equals(check2.getSubject())) ok("7b.update");
        else { no("7b.update", check2); return; }

        // delete
        DBAgent.delete(check2);
        InMailAffair check3 = DBAgent.get(InMailAffair.class, newId);
        if (check3 == null) ok("7c.delete");
        else no("7c.delete", check3);
    }

    // 8. count
    void test8_Count() {
        Map<String, Object> params = new HashMap<>();
        params.put("mid", 1L);
        long c = DBAgent.count("FROM InMailAffair WHERE memberId=:mid", params);
        if (c >= 4) ok("8.count(" + c + ")");
        else no("8.count", c);
    }

    // ==================== 阶段3适配层测试 ====================

    void test9_AppContextBean() {
        Object oa = AppContext.getBean("orgManager");
        if (oa != null && oa instanceof OrgService) ok("9.AppContext.getBean(orgManager)");
        else no("9.AppContext.getBean", oa);
    }

    void test10_AppContextUser() {
        // 模拟JWT Filter设置上下文（/devtest无auth，需手动设置）
        com.email.security.UserContext ctx = new com.email.security.UserContext();
        ctx.setUserId(1L); ctx.setLoginName("admin"); ctx.setUserName("管理员");
        ctx.setDepartmentId(1L); ctx.setAccountId(1L);
        com.email.security.UserContextHolder.set(ctx);
        try {
            UserInfo u = AppContext.getCurrentUser();
            if (u != null && u.getId() == 1L && "admin".equals(u.getLoginName()))
                ok("10.AppContext.getCurrentUser(admin)");
            else no("10.AppContext.getUser", u);
        } finally {
            com.email.security.UserContextHolder.clear();
        }
    }

    void test11_OrgServiceResolve() {
        // 解析 "Member|1" -> 应返回 admin 用户
        Object resolved = orgService.getEntity("Member|1");
        if (resolved != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) resolved;
            if ("管理员".equals(m.get("name")) || "admin".equals(m.get("loginName")))
                ok("11.OrgService.getEntity(Member|1)");
            else no("11.OrgService.getEntity", m);
        } else {
            no("11.OrgService.getEntity", "null");
        }
    }

    void test12_OrgHelperStatic() {
        OrgMember m = OrgHelper.getMember(1L);
        if (m != null && "admin".equals(m.getLoginName()))
            ok("12.OrgHelper.getMember(1)=" + m.getName());
        else no("12.OrgHelper.getMember", m);
    }
}
