package com.email.service;

import com.email.entity.EmailAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface AttachmentService {

    /** 上传附件到 MinIO，返回记录 */
    EmailAttachment upload(MultipartFile file, Long summaryId);

    /** 获取附件的预签名下载URL */
    String getDownloadUrl(Long attachmentId);

    /** 获取附件的输入流（服务端下载用） */
    InputStream download(Long attachmentId);

    /** 获取邮件关联的所有附件 */
    List<EmailAttachment> listBySummaryId(Long summaryId);

    /** 批量删除附件 */
    void deleteByIds(List<Long> ids);

    /** 获取附件实体 */
    EmailAttachment getById(Long id);
}
