package com.email.stub;

import com.email.platform.DBAgent;
import com.email.platform.entity.OrgMember;
import com.email.platform.entity.OrgUnit;

import java.util.*;

/**
 * OA OrgManager 适配 —— 让 InternalMailManager 和 NewEmailUtils 中的 orgManager.xxx() 调用直接工作。
 * 所有方法签名对齐 OA com.seeyon.ctp.organization.manager.OrgManager。
 */
public class OrgManagerAdapter {

    /** getMemberById(Long) → V3x Member */
    public V3xOrgAdapter.Member getMemberById(Long id) {
        if (id == null) return null;
        OrgMember m = DBAgent.get(OrgMember.class, id);
        return m != null ? new V3xOrgAdapter.Member(m) : null;
    }

    /** getDepartmentById(Long) → V3x Department */
    public V3xOrgAdapter.Department getDepartmentById(Long id) {
        if (id == null) return null;
        OrgUnit d = DBAgent.get(OrgUnit.class, id);
        return d != null ? new V3xOrgAdapter.Department(d) : null;
    }

    /** getAccountById(Long) → V3x Account */
    public V3xOrgAdapter.Account getAccountById(Long id) {
        V3xOrgAdapter.Account a = new V3xOrgAdapter.Account();
        a.setId(id != null ? id : 0L); a.setName("单位");
        return a;
    }

    /** getEntity("Member|123") → V3x Entity */
    public V3xOrgAdapter.Entity getEntity(String fullId) {
        return V3xOrgAdapter.getEntity(fullId);
    }

    /** getEntities(ids) → List<V3x Entity> */
    public List<V3xOrgAdapter.Entity> getEntities(String ids) {
        return V3xOrgAdapter.getEntities(ids);
    }

    /** getMembersByTypeAndIds(ids) → Set<V3x Member> */
    public Set<V3xOrgAdapter.Member> getMembersByTypeAndIds(String ids) {
        return V3xOrgAdapter.getMembersByTypeAndIds(ids);
    }

    /** getAllMembers(accountId) */
    public List<V3xOrgAdapter.Member> getAllMembers(Long accountId) {
        Map<String, Object> p = new HashMap<>(); p.put("acctId", accountId);
        List<OrgMember> list = DBAgent.find("FROM OrgMember WHERE accountId=:acctId AND enabled=1", p);
        List<V3xOrgAdapter.Member> ret = new ArrayList<>();
        if (list != null) for (OrgMember m : list) ret.add(new V3xOrgAdapter.Member(m));
        return ret;
    }

    /** getMembersByDepartmentRole(departmentId, roleName) */
    public List<V3xOrgAdapter.Member> getMembersByDepartmentRole(Long deptId, String roleName) {
        return new ArrayList<>();
    }

    /** getTeamMember(teamId) */
    public List<V3xOrgAdapter.Entity> getTeamMember(Long teamId) { return Collections.emptyList(); }

    /** getTeamsByMember(memberId, accountId) */
    public List<?> getTeamsByMember(Long memberId, Long accountId) { return Collections.emptyList(); }

    /** getUserDomainIDs */
    public List<Long> getUserDomainIDs(Long memberId, Long accountId, String type) { return Collections.emptyList(); }

    /** getRootAccount */
    public Object getRootAccount(Long accountId) { return null; }

    /** accessableAccounts */
    public List<V3xOrgAdapter.Account> accessableAccounts(Long memberId) { return Collections.emptyList(); }
}
