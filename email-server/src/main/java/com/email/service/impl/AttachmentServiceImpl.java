package com.email.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.email.entity.EmailAttachment;
import com.email.exception.BusinessException;
import com.email.mapper.EmailAttachmentMapper;
import com.email.security.UserContextHolder;
import com.email.service.AttachmentService;
import com.email.util.MinioUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AttachmentServiceImpl implements AttachmentService {

    @Resource private EmailAttachmentMapper attachmentMapper;
    @Resource private MinioUtil minioUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmailAttachment upload(MultipartFile file, Long summaryId) {
        String minioPath = minioUtil.upload(file);

        EmailAttachment att = new EmailAttachment();
        att.setSummaryId(summaryId);
        att.setFileName(file.getOriginalFilename());
        att.setFileSize(file.getSize());
        att.setMimeType(file.getContentType());
        att.setMinioPath(minioPath);
        att.setCreateTime(LocalDateTime.now());
        att.setCreateBy(UserContextHolder.getUserId());

        attachmentMapper.insert(att);
        return att;
    }

    @Override
    public String getDownloadUrl(Long attachmentId) {
        EmailAttachment att = attachmentMapper.selectById(attachmentId);
        if (att == null) throw new BusinessException("附件不存在");
        return minioUtil.getPresignedUrl(att.getMinioPath());
    }

    @Override
    public InputStream download(Long attachmentId) {
        EmailAttachment att = attachmentMapper.selectById(attachmentId);
        if (att == null) throw new BusinessException("附件不存在");
        return minioUtil.download(att.getMinioPath());
    }

    @Override
    public List<EmailAttachment> listBySummaryId(Long summaryId) {
        LambdaQueryWrapper<EmailAttachment> w = new LambdaQueryWrapper<>();
        w.eq(EmailAttachment::getSummaryId, summaryId);
        return attachmentMapper.selectList(w);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Long> ids) {
        for (Long id : ids) {
            EmailAttachment att = attachmentMapper.selectById(id);
            if (att != null) {
                minioUtil.delete(att.getMinioPath());
                attachmentMapper.deleteById(id);
            }
        }
    }

    @Override
    public EmailAttachment getById(Long id) {
        return attachmentMapper.selectById(id);
    }
}
