package com.email.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮件事务实体 —— 每个收件人一条记录（从 OA InMailAffair PO 迁移）。
 *
 * <p>一条邮件(EmailSummary)对应 N 条事务(EmailAffair)，每个收件人（主送、抄送、发件人自己）一条。
 * 发件人自己也会有一条事务记录，状态为 sent。以此实现收件箱/发件箱/草稿箱共用一张表。</p>
 */
@Data
@TableName("email_affair")
public class EmailAffair {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联的邮件ID */
    private Long summaryId;

    /** 收件人ID（发件箱场景下 = senderId） */
    private Long memberId;

    /** 收件人名称 */
    private String memberName;

    /** 发件人ID */
    private Long senderId;

    /** 发件人名称 */
    private String senderName;

    /** 邮件标题(冗余) */
    private String subject;

    /** 状态: 0=收件箱 1=草稿 2=已发送 3=已删除 5=加密箱 */
    private Integer state;

    /** 是否彻底删除 */
    private Integer isDelete;

    /** 删除前状态（用于恢复） */
    private Integer deleteState;

    /** 是否已读 0=未读 1=已读 */
    private Integer readFlag;

    /** 阅读时间 */
    private LocalDateTime browseTime;

    /** 是否已办理 0=否 1=是 */
    private Integer isHandled;

    /** 是否收藏 0=否 1=是 */
    private Integer collect;

    /** 是否有附件 */
    private Integer attachmentsFlag;

    /** 邮件大小 */
    private Long affairSize;

    /** 是否转发过 */
    private Integer isForward;

    /** 是否回复过 */
    private Integer isReply;

    /** 回复类型 */
    private Integer replyType;

    /** 审批是否通过 */
    private Integer passTheAudit;

    /** 自定义文件夹路径 */
    private String path;

    /** 发件人部门ID */
    private Long orgDepartmentId;

    /** 发件人单位ID */
    private Long orgAccountId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 转发人名称 */
    private String forwardMember;

    // ---- 非数据库字段 ----

    /** 密级ID字符串（前端需要） */
    @TableField(exist = false)
    private transient String secretIdStr;

    /** 密级名称（前端需要） */
    @TableField(exist = false)
    private transient String secretNameStr;

    /** 头像路径（前端需要） */
    @TableField(exist = false)
    private transient String headImg;

    /** 附件预览标记（Office 文件转换后） */
    @TableField(exist = false)
    private transient Integer showPreview;
}
