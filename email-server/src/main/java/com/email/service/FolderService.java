package com.email.service;

import com.email.entity.EmailFolder;

import java.util.List;

/**
 * 文件夹服务（从 OA FolderMailManager 迁移）。
 */
public interface FolderService {

    /** 获取用户的所有自定义文件夹 */
    List<EmailFolder> getFolders(Long memberId);

    /** 创建文件夹 */
    EmailFolder create(Long memberId, String fileName, String type, String parentPath);

    /** 重命名 */
    void rename(Long folderId, String newName);

    /** 删除 */
    void delete(Long folderId);

    /** 移动邮件到文件夹 */
    void moveMails(List<Long> affairIds, Long folderId);

    /** 更新文件夹过滤规则 */
    void updateRule(Long folderId, String rule);

    /** 将规则应用到历史邮件 */
    void applyRuleToHistory(Long folderId);
}
