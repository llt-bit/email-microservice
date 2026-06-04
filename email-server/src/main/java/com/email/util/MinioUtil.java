package com.email.util;

import com.email.config.MinioConfig;
import com.email.exception.BusinessException;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 文件存储工具（替代 OA 的 FileManager/AttachmentManager）。
 * MinIO 优先，不可用时自动降级到本地磁盘。
 */
@Component
public class MinioUtil {
    private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(MinioUtil.class);

    private final MinioClient minioClient;
    private final MinioConfig config;
    private boolean minioAvailable = true;
    private final String localBase;

    public MinioUtil(MinioClient minioClient, MinioConfig config) {
        this.minioClient = minioClient;
        this.config = config;
        this.localBase = System.getProperty("java.io.tmpdir") + "/email-attachments";
        ensureBucket();
    }

    private void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(config.getBucket()).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(config.getBucket()).build());
            }
            log.info("MinIO 连接成功");
        } catch (Exception e) {
            minioAvailable = false;
            new File(localBase).mkdirs();
            log.warn("MinIO 不可用，附件改用本地磁盘: " + localBase);
        }
    }

    /** 上传文件 */
    public String upload(MultipartFile file) {
        try {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) ext = originalName.substring(originalName.lastIndexOf("."));
            String objectName = datePath + "/" + UUID.randomUUID().toString() + ext;

            if (minioAvailable) {
                try {
                    minioClient.putObject(PutObjectArgs.builder().bucket(config.getBucket()).object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build());
                } catch (Exception e) {
                    log.warn("MinIO 上传失败，降级到本地: " + e.getMessage());
                    writeLocal(objectName, file.getInputStream());
                }
            } else {
                writeLocal(objectName, file.getInputStream());
            }
            return objectName;
        } catch (Exception e) {
            throw new BusinessException("附件上传失败: " + e.getMessage());
        }
    }

    /** 上传 InputStream */
    public String upload(InputStream is, String fileName, String contentType, long size) {
        try {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String ext = "";
            if (fileName.contains(".")) ext = fileName.substring(fileName.lastIndexOf("."));
            String objectName = datePath + "/" + UUID.randomUUID().toString() + ext;

            if (minioAvailable) {
                try {
                    minioClient.putObject(PutObjectArgs.builder().bucket(config.getBucket()).object(objectName)
                            .stream(is, size, -1).contentType(contentType).build());
                } catch (Exception e) {
                    writeLocal(objectName, is);
                }
            } else {
                writeLocal(objectName, is);
            }
            return objectName;
        } catch (Exception e) {
            throw new BusinessException("附件上传失败: " + e.getMessage());
        }
    }

    /** 获取预签名下载 URL（本地模式返回文件路径） */
    public String getPresignedUrl(String objectName) {
        if (minioAvailable) {
            try {
                return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                        .bucket(config.getBucket()).object(objectName).method(Method.GET)
                        .expiry(config.getPresignExpiry(), TimeUnit.SECONDS).build());
            } catch (Exception e) { /* fallback */ }
        }
        return objectName; // MinIO 不可用时返回路径（需由 Controller 服务端中转下载）
    }

    /** 下载文件流 */
    public InputStream download(String objectName) {
        if (minioAvailable) {
            try {
                return minioClient.getObject(GetObjectArgs.builder().bucket(config.getBucket()).object(objectName).build());
            } catch (Exception e) { /* fallback */ }
        }
        try {
            return new FileInputStream(new File(localBase, objectName));
        } catch (Exception e) {
            throw new BusinessException("附件下载失败");
        }
    }

    /** 删除文件 */
    public void delete(String objectName) {
        if (minioAvailable) {
            try { minioClient.removeObject(RemoveObjectArgs.builder().bucket(config.getBucket()).object(objectName).build()); return; } catch (Exception e) {}
        }
        new File(localBase, objectName).delete();
    }

    /** 本地文件写入 */
    private void writeLocal(String objectName, InputStream is) throws IOException {
        File f = new File(localBase, objectName);
        f.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(f)) {
            byte[] buf = new byte[8192]; int len;
            while ((len = is.read(buf)) > 0) fos.write(buf, 0, len);
        }
    }
}
