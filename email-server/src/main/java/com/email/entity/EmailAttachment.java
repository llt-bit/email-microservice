package com.email.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮件附件记录（从 OA Attachment/V3XFile 剥离）。
 */
@Data
@TableName("email_attachment")
public class EmailAttachment {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联的邮件ID */
    private Long summaryId;

    /** 原始文件名 */
    private String fileName;

    /** 文件大小(字节) */
    private Long fileSize;

    /** MIME类型 */
    private String mimeType;

    /** MinIO存储路径(格式: yyyy/MM/dd/uuid.ext) */
    private String minioPath;

    /** 是否已转换为预览格式 */
    private Integer isTransfered;

    /** 上传时间 */
    private LocalDateTime createTime;

    /** 上传人ID */
    private Long createBy;
}
