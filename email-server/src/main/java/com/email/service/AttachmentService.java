package com.email.service;

import com.email.platform.DBAgent;
import com.email.stub.OaCompat.Attachment;
import com.email.stub.OaCompat.AttachmentManagerAdapter;
import com.email.util.MinioUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;

/**
 * 附件服务 - 上传到 MinIO，元数据存 email_attachment 表。
 * 同时作为 AttachmentManagerAdapter 的真实实现，让 OA 代码的调用生效。
 */
@Service("attachmentManager")
public class AttachmentService {

    private static final Log log = LogFactory.getLog(AttachmentService.class);

    @Resource private MinioUtil minioUtil;

    /** 上传附件到 MinIO，返回新 Attachment 记录 */
    public Attachment upload(MultipartFile file) {
        String minioPath = minioUtil.upload(file);
        Attachment att = new Attachment();
        Long id = com.email.platform.UUIDLong.longValue();
        att.setId(id);
        att.setFilename(file.getOriginalFilename());
        att.setSize(file.getSize());
        att.setMimeType(file.getContentType());

        // 保存到 email_attachment 表（原生 SQL INSERT，用 JDBCAgent 执行）
        List<Object> params = new ArrayList<>();
        params.add(id); params.add(null); params.add(file.getOriginalFilename());
        params.add(file.getSize()); params.add(file.getContentType()); params.add(minioPath);
        params.add(new Date());
        try {
            com.email.platform.JDBCAgent agent = new com.email.platform.JDBCAgent();
            agent.execute("INSERT INTO email_attachment (id, summary_id, file_name, file_size, mime_type, minio_path, create_time) VALUES (?,?,?,?,?,?,?)", params);
            agent.close();
        } catch(Exception e) { log.error("附件入库失败", e); }

        log.info("附件上传: " + file.getOriginalFilename() + " -> " + minioPath);
        return att;
    }

    /** 获取邮件的附件列表 */
    @SuppressWarnings("unchecked")
    public List<Attachment> listBySummaryId(Long summaryId) {
        Map<String, Object> p = new HashMap<>();
        p.put("sid", summaryId);
        List<Object[]> rows = DBAgent.find("SELECT id, summary_id, file_name, file_size, mime_type, minio_path, create_time FROM email_attachment WHERE summary_id=:sid", p);
        List<Attachment> result = new ArrayList<>();
        for (Object[] row : rows) {
            Attachment att = new Attachment();
            att.setId(((Number) row[0]).longValue());
            att.setFilename((String) row[2]);
            att.setSize(((Number) row[3]).longValue());
            att.setMimeType((String) row[4]);
            att.setFileUrl(((Number) row[0]).longValue());
            result.add(att);
        }
        return result;
    }

    /** 获取附件记录 */
    public Attachment getById(Long id) {
        Map<String, Object> p = new HashMap<>(); p.put("id", id);
        List<?> rows = DBAgent.find("SELECT id, file_name, file_size, mime_type, minio_path FROM email_attachment WHERE id=:id", p);
        if (rows.isEmpty()) return null;
        Object[] row = (Object[]) rows.get(0);
        Attachment att = new Attachment();
        att.setId(((Number) row[0]).longValue());
        att.setFilename((String) row[1]);
        att.setSize(((Number) row[2]).longValue());
        att.setMimeType((String) row[3]);
        att.setFileUrl(((Number) row[0]).longValue());
        return att;
    }

    /** 获取 MinIO 预签名下载 URL */
    public String getDownloadUrl(Long id) {
        Map<String, Object> p = new HashMap<>(); p.put("id", id);
        List<?> rows = DBAgent.find("SELECT minio_path FROM email_attachment WHERE id=:id", p);
        if (rows.isEmpty()) return null;
        String path = (String) ((Object[]) rows.get(0))[0];
        return minioUtil.getPresignedUrl(path);
    }

    /** 删除附件 */
    public void deleteById(Long id) {
        Map<String, Object> p = new HashMap<>(); p.put("id", id);
        List<?> rows = DBAgent.find("SELECT minio_path FROM email_attachment WHERE id=:id", p);
        if (!rows.isEmpty()) {
            String path = (String) ((Object[]) rows.get(0))[0];
            minioUtil.delete(path);
        }
        DBAgent.bulkUpdate("DELETE FROM email_attachment WHERE id=:id", p);
    }
}
