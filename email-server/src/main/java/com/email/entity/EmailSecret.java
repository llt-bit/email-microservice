package com.email.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 加密密码箱（从 OA InMailSecret PO 迁移）。
 */
@Data
@TableName("email_secret")
public class EmailSecret {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long memberId;
    private String loginName;
    private String pwd;
    private Integer state;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String ext1;
    private String ext2;
    private String ext3;
    private String ext4;
    private String ext5;
}
