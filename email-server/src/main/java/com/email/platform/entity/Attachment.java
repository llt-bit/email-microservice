package com.email.platform.entity;

/**
 * 附件实体 —— 映射 email_attachment 表。
 * HQL 中 "Attachment as a" / "a.subReference, a.filename" 用于附件名搜索。
 */
public class Attachment {
    private Long id;
    private Long subReference;   // = summary_id（OA 中关联邮件ID）
    private String filename;     // = file_name

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSubReference() { return subReference; }
    public void setSubReference(Long subReference) { this.subReference = subReference; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
}
