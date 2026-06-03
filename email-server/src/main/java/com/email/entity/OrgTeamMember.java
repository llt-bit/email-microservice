package com.email.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 团队成员关系表。
 */
@Data
@TableName("org_team_member")
public class OrgTeamMember {

    @TableId(type = IdType.INPUT)
    private Long teamId;

    private Long memberId;
}
