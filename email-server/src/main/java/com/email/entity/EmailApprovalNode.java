package com.email.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批节点（一个审批可以有多个节点/多个审批人）。
 */
@Data
@TableName("email_approval_node")
public class EmailApprovalNode {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long approvalId;
    private Long approverId;
    private String approverName;
    /** 节点状态: PENDING / APPROVED / REJECTED */
    private String status;
    private String comment;
    private LocalDateTime operateTime;
    /** 审批顺序 */
    private Integer sort;
}
