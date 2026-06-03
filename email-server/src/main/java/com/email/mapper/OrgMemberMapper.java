package com.email.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.email.entity.OrgMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrgMemberMapper extends BaseMapper<OrgMember> {

    OrgMember findByLoginName(@Param("loginName") String loginName);

    List<OrgMember> findByDepartmentId(@Param("departmentId") Long departmentId);

    List<OrgMember> findByAccountId(@Param("accountId") Long accountId);

    List<OrgMember> searchByName(@Param("keyword") String keyword);

    /** 按更新时间增量同步 */
    List<OrgMember> findByUpdateTimeAfter(@Param("updateTime") String updateTime);
}
