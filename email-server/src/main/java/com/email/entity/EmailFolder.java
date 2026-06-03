package com.email.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 自定义文件夹（从 OA InMailFolder PO 迁移）。
 */
@Data
@TableName("email_folder")
public class EmailFolder {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属人员ID */
    private Long memberId;

    /** 文件夹名称 */
    private String fileName;

    /** 文件夹完整路径 */
    private String path;

    /** 类型: inBox/sent */
    private String type;

    /** 是否删除 */
    private Integer isDelete;

    /** 过滤规则(JSON格式) */
    private String rule;

    /** 创建时间 */
    private LocalDateTime createTime;
}
