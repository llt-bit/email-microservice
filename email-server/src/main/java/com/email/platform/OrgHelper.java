package com.email.platform;

import com.email.platform.entity.OrgMember;
import com.email.platform.entity.OrgUnit;
import com.email.service.OrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * OrgHelper 兼容类 —— 对齐 OA com.seeyon.ctp.organization.dao.OrgHelper。
 * 提供统一的静态方法，邮件代码调用 OrgHelper.xxx() 不用改一行代码。
 */
@Component
public class OrgHelper {

    @Autowired
    private OrgService orgService;

    private static OrgService staticService;

    @PostConstruct
    public void init() {
        staticService = orgService;
        OrgService.setInstance(orgService);
    }

    /** 对齐 OA OrgHelper.getMember(id) */
    public static OrgMember getMember(Long id) {
        return staticService != null ? staticService.getMemberById(id) : null;
    }

    /** 对齐 OA OrgHelper.getDepartment(id) */
    public static OrgUnit getDepartment(Long id) {
        return staticService != null ? staticService.getDepartmentById(id) : null;
    }

    /** 对齐 OA OrgHelper.checkLevelScope(userId, memberId) */
    public static boolean checkLevelScope(Long userId, Long memberId) {
        // 简化实现：OA 密级管控暂不独立实现，直接放行
        return true;
    }
}
