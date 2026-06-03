package com.email.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 组织架构-人员（从 OA V3xOrgMember 同步）。
 */
@Data
@TableName("org_member")
public class OrgMember {

    @TableId(type = IdType.INPUT)
    private Long id;
    private String name;
    private String loginName;
    private String email;
    private Long departmentId;
    private Long accountId;
    private Integer enabled;
    private Integer isDeleted;
    private Long secretLevelId;
    private LocalDateTime updateTime;
}
