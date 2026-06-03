package com.email.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.email.entity.EmailAffair;
import com.email.entity.EmailFolder;
import com.email.exception.BusinessException;
import com.email.mapper.EmailAffairMapper;
import com.email.mapper.EmailFolderMapper;
import com.email.security.UserContextHolder;
import com.email.service.FolderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 自定义文件夹服务实现（从 OA FolderMailManagerImpl 迁移）。
 */
@Slf4j
@Service
public class FolderServiceImpl implements FolderService {

    @Resource private EmailFolderMapper folderMapper;
    @Resource private EmailAffairMapper affairMapper;

    @Override
    public List<EmailFolder> getFolders(Long memberId) {
        return folderMapper.findByMemberId(memberId, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmailFolder create(Long memberId, String fileName, String type, String parentPath) {
        // 构造完整路径
        String fullPath = (parentPath != null ? parentPath + "/" : "") + fileName;

        // 检查同名
        EmailFolder exist = folderMapper.findByMemberIdAndPath(memberId, fullPath);
        if (exist != null) {
            throw new BusinessException("文件夹已存在: " + fileName);
        }

        EmailFolder f = new EmailFolder();
        f.setMemberId(memberId);
        f.setFileName(fileName);
        f.setPath(fullPath);
        f.setType(type);
        f.setIsDelete(0);
        f.setCreateTime(LocalDateTime.now());

        folderMapper.insert(f);
        return f;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rename(Long folderId, String newName) {
        EmailFolder f = folderMapper.selectById(folderId);
        if (f == null) throw new BusinessException("文件夹不存在");
        f.setFileName(newName);
        folderMapper.updateById(f);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long folderId) {
        EmailFolder f = folderMapper.selectById(folderId);
        if (f == null) throw new BusinessException("文件夹不存在");

        // 软删除
        f.setIsDelete(1);
        folderMapper.updateById(f);

        // 清理关联邮件的 path 字段
        LambdaQueryWrapper<EmailAffair> w = new LambdaQueryWrapper<>();
        w.eq(EmailAffair::getPath, f.getPath());
        List<EmailAffair> affairs = affairMapper.selectList(w);
        for (EmailAffair a : affairs) {
            a.setPath(null);
            affairMapper.updateById(a);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveMails(List<Long> affairIds, Long folderId) {
        EmailFolder f = folderMapper.selectById(folderId);
        if (f == null) throw new BusinessException("目标文件夹不存在");

        List<EmailAffair> affairs = affairMapper.selectBatchIds(affairIds);
        for (EmailAffair a : affairs) {
            if (a != null) {
                a.setPath(f.getPath());
                affairMapper.updateById(a);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRule(Long folderId, String rule) {
        EmailFolder f = folderMapper.selectById(folderId);
        if (f == null) throw new BusinessException("文件夹不存在");
        f.setRule(rule);
        folderMapper.updateById(f);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRuleToHistory(Long folderId) {
        EmailFolder f = folderMapper.selectById(folderId);
        if (f == null || f.getRule() == null) return;

        String rule = f.getRule();
        Long userId = UserContextHolder.getUserId();

        // 拉取该用户的所有收件箱邮件
        LambdaQueryWrapper<EmailAffair> w = new LambdaQueryWrapper<>();
        w.eq(EmailAffair::getMemberId, userId)
         .in(EmailAffair::getState, 0, 2)
         .isNull(EmailAffair::getPath);
        List<EmailAffair> affairs = affairMapper.selectList(w);

        // 规则简单匹配
        for (EmailAffair a : affairs) {
            if (a.getSubject() != null && a.getSubject().contains(rule)) {
                a.setPath(f.getPath());
                affairMapper.updateById(a);
            } else if (a.getSenderName() != null && a.getSenderName().contains(rule)) {
                a.setPath(f.getPath());
                affairMapper.updateById(a);
            }
        }
    }
}
