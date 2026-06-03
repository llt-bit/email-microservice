package com.email.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮件密级审批（自建，替代 OA CAP4 工作流）。
 */
@Data
@TableName("email_approval")
public class EmailApproval {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long summaryId;
    private Long secretLevelId;
    private String secretLevelName;
    /** 审批状态: PENDING / APPROVED / REJECTED */
    private String status;
    private Long applicantId;
    private String applicantName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
