package com.email.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * OA 数据库只读数据源（用于同步组织架构）。
 *
 * <p>仅在 {@code email.org-sync.enabled=true} 时激活。</p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "email.org-sync.oa-datasource")
@ConditionalOnProperty(name = "email.org-sync.enabled", havingValue = "true", matchIfMissing = true)
public class OaDataSourceConfig {

    private String driverClassName = "oracle.jdbc.OracleDriver";
    private String url;
    private String username;
    private String password;

    /**
     * OA 只读数据源（仅用于同步组织架构）。
     * 命名为 "oaDataSource"，便于 OrgService 注入。
     */
    @Bean(name = "oaDataSource")
    public DataSource oaDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(driverClassName);
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setMinimumIdle(1);
        ds.setMaximumPoolSize(3);
        ds.setConnectionTimeout(10000);
        ds.setIdleTimeout(300000);
        ds.setPoolName("OADataSource");
        return ds;
    }

    /**
     * 邮件业务主数据源。
     * Spring Boot 自动配置，这里仅在存在 OA 数据源时保留 @Primary。
     */
    @Primary
    @Bean(name = "emailDataSource")
    @ConditionalOnProperty(name = "email.org-sync.enabled", havingValue = "false")
    public DataSource emailDataSource() {
        // 让 Spring Boot 自动配置处理，这里只是占位
        return null;
    }
}
