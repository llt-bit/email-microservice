package com.email.enums;

/**
 * 邮件文件夹/视图类型（前端 Vuex store 中 menus 使用的 type）。
 */
public enum MailType {

    INBOX("inBox", "收件箱"),
    DRAFT("draft", "草稿箱"),
    SENT("sent", "发件箱"),
    DELETE("delete", "已删除"),
    FLAG("flag", "星标邮件"),
    ENCRYPTION("encryption", "加密箱"),
    COLLECTION("collection", "收藏"),
    CUSTOM_FOLDER("customFolder", "自定义文件夹");

    private final String type;
    private final String label;

    MailType(String type, String label) {
        this.type = type;
        this.label = label;
    }

    public String getType() { return type; }
    public String getLabel() { return label; }
}
