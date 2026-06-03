package com.email.config;

import com.email.platform.DBAgent;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Hibernate 5 配置 —— 替代 MyBatis，使 OA 的 hbm.xml 和 HQL 可原样复用。
 */
@Configuration
@EnableTransactionManagement
public class HibernateConfig {

    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean sfb = new LocalSessionFactoryBean();
        sfb.setDataSource(dataSource);

        // 扫描邮件模块的 hbm.xml（后续从 OA 复制）
        sfb.setMappingLocations(
                new org.springframework.core.io.ClassPathResource("hbm/InMailSummary.hbm.xml"),
                new org.springframework.core.io.ClassPathResource("hbm/InMailAffair.hbm.xml"),
                new org.springframework.core.io.ClassPathResource("hbm/InMailSecret.hbm.xml"),
                new org.springframework.core.io.ClassPathResource("hbm/InMailSummaryMembers.hbm.xml"),
                new org.springframework.core.io.ClassPathResource("hbm/MyQuery.hbm.xml"),
                // 平台实体 —— HQL JOIN 时需要
                new org.springframework.core.io.ClassPathResource("hbm/OrgMember.hbm.xml"),
                new org.springframework.core.io.ClassPathResource("hbm/OrgUnit.hbm.xml"),
                new org.springframework.core.io.ClassPathResource("hbm/Attachment.hbm.xml")
        );

        Properties props = new Properties();
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQL57InnoDBDialect");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "false");
        props.put("hibernate.hbm2ddl.auto", "none");
        // 允许 HQL 中的跨类 JOIN（OA 大量使用）
        props.put("hibernate.query.startup_check", "false");
        sfb.setHibernateProperties(props);

        return sfb;
    }

    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("sessionFactory") SessionFactory sessionFactory) {
        HibernateTransactionManager tm = new HibernateTransactionManager();
        tm.setSessionFactory(sessionFactory);
        return tm;
    }

    /**
     * 注入 SessionFactory 到 DBAgent，使所有静态方法可用。
     */
    @Bean
    public Object dbaInit(@Qualifier("sessionFactory") SessionFactory sf) {
        DBAgent.setSessionFactory(sf);
        return new Object();
    }
}
