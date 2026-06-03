package com.email.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * OA 数据库只读数据源（用于同步组织架构）。
 * 仅在配置了 OA 数据库连接信息时激活。
 */
@Configuration
@ConfigurationProperties(prefix = "email.org-sync.oa-datasource")
@ConditionalOnExpression("!'${email.org-sync.oa-datasource.url:}'.isEmpty()")
public class OaDataSourceConfig {

    private String driverClassName = "oracle.jdbc.OracleDriver";
    private String url;
    private String username;
    private String password;

    public void setDriverClassName(String v) { this.driverClassName = v; }
    public void setUrl(String v) { this.url = v; }
    public void setUsername(String v) { this.username = v; }
    public void setPassword(String v) { this.password = v; }

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
}
