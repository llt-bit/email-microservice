package com.email.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.sql.Timestamp;

/**
 * 加密密码箱 PO —— 从 OA InMailSecret 直接复制。
 */
@TableName("email_secret")
public class InMailSecret extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("member_id")
    private Long memberId;
    @TableField("login_name")
    private String loginName;
    private String pwd;
    private Integer state;
    @TableField("create_time")
    private Timestamp createDate;
    @TableField("update_time")
    private Timestamp updateDate;
    private String ext1;
    private String ext2;
    private String ext3;
    private String ext4;
    private String ext5;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getLoginName() { return loginName; }
    public void setLoginName(String loginName) { this.loginName = loginName; }
    public String getPwd() { return pwd; }
    public void setPwd(String pwd) { this.pwd = pwd; }
    public Integer getState() { return state; }
    public void setState(Integer state) { this.state = state; }
    public Timestamp getCreateDate() { return createDate; }
    public void setCreateDate(Timestamp createDate) { this.createDate = createDate; }
    public Timestamp getUpdateDate() { return updateDate; }
    public void setUpdateDate(Timestamp updateDate) { this.updateDate = updateDate; }
    public String getExt1() { return ext1; } public void setExt1(String ext1) { this.ext1 = ext1; }
    public String getExt2() { return ext2; } public void setExt2(String ext2) { this.ext2 = ext2; }
    public String getExt3() { return ext3; } public void setExt3(String ext3) { this.ext3 = ext3; }
    public String getExt4() { return ext4; } public void setExt4(String ext4) { this.ext4 = ext4; }
    public String getExt5() { return ext5; } public void setExt5(String ext5) { this.ext5 = ext5; }
}
