package com.email;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 内部邮件独立微服务启动类
 *
 * <p>从致远 OA V8 的 internalmail 模块剥离而来，独立部署、独立数据库、独立文件存储。
 * OA 宕机不影响邮件功能。</p>
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
@MapperScan("com.email.mapper")
public class EmailApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailApplication.class, args);
    }
}
