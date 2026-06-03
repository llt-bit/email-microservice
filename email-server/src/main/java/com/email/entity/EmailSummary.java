package com.email.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮件主体实体（从 OA InMailSummary PO 迁移）。
 */
@Data
@TableName("email_summary")
public class EmailSummary {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 标题 */
    private String subject;

    /** 状态: 0=草稿 1=已发送 2=已删除 */
    private Integer state;

    /** 发送人ID */
    private Long senderId;

    /** 发送人名 */
    private String senderName;

    /** 发送人部门ID */
    private Long orgDepartmentId;

    /** 发送人单位ID */
    private Long orgAccountId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 主送人名称列表 */
    private String sendTo;

    /** 主送人ID列表 "Member|123,Department|456" */
    private String sendToIds;

    /** 抄送人名称列表 */
    private String copyTo;

    /** 抄送人ID列表 */
    private String copyToIds;

    /** 邮件正文(HTML) */
    @TableField("summary_content")
    private String summaryContent;

    /** 正文大小 */
    private Long summarySize;

    /** 是否有附件 0=无 1=有 */
    private Integer attachmentsFlag;

    /** 附件名称列表(冗余) */
    private String attachments;

    /** 标志: forward/reply/allReply/reEdit */
    private String mark;

    /** 回复人名称 */
    private String replyMember;

    /** 回复人ID */
    private Long replyMemberId;

    /** 被回复邮件事项ID */
    private Long replyParentAffairId;

    /** 被回复邮件summaryId */
    private Long replyParentSummaryId;

    /** 转发人名称 */
    private String forwardMember;

    /** 转发人ID */
    private Long forwardMemberId;

    /** 被转发邮件事项ID */
    private Long parentformAffairId;

    /** 被转发邮件summaryId */
    private Long parentformSummaryId;

    /** 是否可转发 */
    private Integer canForward;

    /** 是否可编辑附件 */
    private Integer canEditAttachment;

    /** 是否定时发送 0=否 1=是 */
    private Integer isTiming;

    /** 定时发送时间 */
    private LocalDateTime timingDate;

    /** 是否可导出 */
    private Integer isCanExport;

    /** 是否跨网 */
    private Integer kuaWang;

    /** 数据摆渡来源 */
    private String ferrySource;

    /** 文件密级ID */
    private Long fileSecretLevelId;

    /** 审批人ID列表 */
    private String approver;

    /** 审批人名称列表 */
    private String approverStr;

    /** 自动保存标记 */
    private String autosave;

    /** 首次自动保存时间 */
    private LocalDateTime firstAutosaveTime;

    /** 发件人详情 */
    private String startDetail;

    /** 主送人详情 */
    private String sendToDetail;

    /** 抄送人详情 */
    private String copyToDetail;

    /** 匿名投诉人ID */
    private Long complaintMemberId;

    /** 短信通知内容 */
    private String shortMsgContent;

    /** 创建人ID */
    private Long createBy;

    // ---- 非数据库字段（从 OA BO 继承） ----
    /** 是否定时发信（前端参数，不存表） */
    @TableField(exist = false)
    private transient Boolean timedTask;

    /** 主送人字符串（前端传入） */
    @TableField(exist = false)
    private transient String receiversStr;

    /** 主送人ID（前端传入） */
    @TableField(exist = false)
    private transient String receivers;

    /** 抄送人名称（前端传入） */
    @TableField(exist = false)
    private transient String copyReceiversStr;

    /** 抄送人ID（前端传入） */
    @TableField(exist = false)
    private transient String copyReceivers;

    /** 文件密级名称(前端传入) */
    @TableField(exist = false)
    private transient String secretName;
}
