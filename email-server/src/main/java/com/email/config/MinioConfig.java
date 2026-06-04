package com.email.config;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 对象存储配置。
 */
@Configuration
@ConfigurationProperties(prefix = "email.minio")
public class MinioConfig {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private int presignExpiry = 900;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
    public String getEndpoint() { return endpoint; } public void setEndpoint(String v) { this.endpoint = v; }
    public String getAccessKey() { return accessKey; } public void setAccessKey(String v) { this.accessKey = v; }
    public String getSecretKey() { return secretKey; } public void setSecretKey(String v) { this.secretKey = v; }
    public String getBucket() { return bucket; } public void setBucket(String v) { this.bucket = v; }
    public int getPresignExpiry() { return presignExpiry; } public void setPresignExpiry(int v) { this.presignExpiry = v; }
}
