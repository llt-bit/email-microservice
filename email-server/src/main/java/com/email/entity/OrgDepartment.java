package com.email.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("org_department")
public class OrgDepartment {

    @TableId(type = IdType.INPUT)
    private Long id;
    private String name;
    private String fullName;
    private Long parentId;
    private Long accountId;
    private Integer sort;
    private Integer isDeleted;
    private LocalDateTime updateTime;
}
