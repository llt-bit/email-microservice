package com.email.entity;

import java.sql.Timestamp;

/**
 * 加密密码箱 PO —— 从 OA InMailSecret 直接复制，使用 Hibernate hbm.xml 映射。
 */
public class InMailSecret {

    private Long id;
    private Long memberId;
    private String loginName;
    private String pwd;
    private Integer state;
    private Timestamp createDate;
    private Timestamp updateDate;
    private String ext1;
    private String ext2;
    private String ext3;
    private String ext4;
    private String ext5;

    public void setIdIfNew() { if (this.id == null) this.id = com.email.platform.UUIDLong.longValue(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getMemberId() { return memberId; } public void setMemberId(Long v) { this.memberId = v; }
    public String getLoginName() { return loginName; } public void setLoginName(String v) { this.loginName = v; }
    public String getPwd() { return pwd; } public void setPwd(String v) { this.pwd = v; }
    public Integer getState() { return state; } public void setState(Integer v) { this.state = v; }
    public Timestamp getCreateDate() { return createDate; } public void setCreateDate(Timestamp v) { this.createDate = v; }
    public Timestamp getUpdateDate() { return updateDate; } public void setUpdateDate(Timestamp v) { this.updateDate = v; }
    public String getExt1() { return ext1; } public void setExt1(String v) { this.ext1 = v; }
    public String getExt2() { return ext2; } public void setExt2(String v) { this.ext2 = v; }
    public String getExt3() { return ext3; } public void setExt3(String v) { this.ext3 = v; }
    public String getExt4() { return ext4; } public void setExt4(String v) { this.ext4 = v; }
    public String getExt5() { return ext5; } public void setExt5(String v) { this.ext5 = v; }
}
