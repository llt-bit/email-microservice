package com.email.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 邮件群组/部门成员展开记录（从 OA InMailSummaryMembersPo 迁移）。
 */
@Data
@TableName("email_summary_members")
public class EmailSummaryMembers {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long mailSummaryId;
    private Long memberId;
    private String memberName;
    private String memberLoginName;
    private String type;
    private Long typeId;
    private String detail;
    private Long accountId;
    private Integer sort;
}
