package com.email.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("org_team")
public class OrgTeam {

    @TableId(type = IdType.INPUT)
    private Long id;
    private String name;
    private Long accountId;
    private Integer isDeleted;
    private LocalDateTime updateTime;
}
