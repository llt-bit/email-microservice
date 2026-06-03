package com.email.entity;

import java.sql.Timestamp;

/**
 * 自定义文件夹 PO —— 从 OA InMailFolder 直接复制。
 * 使用 Hibernate hbm.xml 映射，不需要 MyBatis 注解。
 */
public class InMailFolder {

    private Long id;
    private Long memberId;
    private String path;
    private String type;
    private Integer isDelete;
    private String rule;
    private String fileName;
    private Timestamp createDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getIsDelete() { return isDelete; }
    public void setIsDelete(Integer isDelete) { this.isDelete = isDelete; }
    public String getRule() { return rule; }
    public void setRule(String rule) { this.rule = rule; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Timestamp getCreateDate() { return createDate; }
    public void setCreateDate(Timestamp createDate) { this.createDate = createDate; }
}
