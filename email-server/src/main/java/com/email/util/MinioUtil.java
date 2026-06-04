package com.email.util;

import com.email.config.MinioConfig;
import com.email.exception.BusinessException;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 文件操作工具（替代 OA 的 FileManager/AttachmentManager）。
 */

@Component
public class MinioUtil {
    private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(MinioUtil.class);

    private final MinioClient minioClient;
    private final MinioConfig config;

    public MinioUtil(MinioClient minioClient, MinioConfig config) {
        this.minioClient = minioClient;
        this.config = config;
        ensureBucket();
    }

    private void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(config.getBucket()).build());
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(config.getBucket()).build());
                log.info("MinIO bucket 创建成功: " + config.getBucket());
            }
        } catch (Exception e) {
            log.warn("MinIO 不可用（附件功能暂不可用）: " + e.getMessage());
        }
    }

    /**
     * 上传附件。
     *
     * @param file MultipartFile
     * @return minio 存储路径，格式: yyyy/MM/dd/uuid.ext
     */
    public String upload(MultipartFile file) {
        try {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String objectName = datePath + "/" + UUID.randomUUID().toString() + ext;

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            log.info("附件上传成功: " + objectName + " " + file.getSize() + " bytes");
            return objectName;
        } catch (Exception e) {
            log.error("附件上传失败", e);
            throw new BusinessException("附件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传 InputStream。
     */
    public String upload(InputStream is, String fileName, String contentType, long size) {
        try {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String ext = "";
            if (fileName.contains(".")) {
                ext = fileName.substring(fileName.lastIndexOf("."));
            }
            String objectName = datePath + "/" + UUID.randomUUID().toString() + ext;

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .stream(is, size, -1)
                    .contentType(contentType)
                    .build());

            return objectName;
        } catch (Exception e) {
            log.error("附件上传失败", e);
            throw new BusinessException("附件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取预签名下载 URL（有效期从配置读取，默认 15 分钟）。
     */
    public String getPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(config.getBucket())
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(config.getPresignExpiry(), TimeUnit.SECONDS)
                            .build());
        } catch (Exception e) {
            log.error("获取预签名URL失败: " + objectName, e);
            throw new BusinessException("获取下载链接失败");
        }
    }

    /**
     * 下载文件流。
     */
    public InputStream download(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("附件下载失败: " + objectName, e);
            throw new BusinessException("附件下载失败");
        }
    }

    public void delete(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.warn("附件删除失败: " + objectName, e);
        }
    }
}
