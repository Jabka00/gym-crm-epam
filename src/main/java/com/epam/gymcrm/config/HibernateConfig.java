package com.epam.gymcrm.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@RequiredArgsConstructor
public class HibernateConfig {

    private final DatabaseProperties dbProps;
    private final HibernateProperties hibernateProps;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbProps.getUrl());
        config.setUsername(dbProps.getUsername());
        config.setPassword(dbProps.getPassword());
        config.setDriverClassName(dbProps.getDriver());
        return new HikariDataSource(config);
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan("com.epam.gymcrm.entity");
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalSessionFactoryBean sessionFactory) {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory.getObject());
        return transactionManager;
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", hibernateProps.getDialect());
        properties.put("hibernate.show_sql", hibernateProps.isShowSql());
        properties.put("hibernate.format_sql", hibernateProps.isFormatSql());
        properties.put("hibernate.hbm2ddl.auto", hibernateProps.getHbm2ddlAuto());
        return properties;
    }
}
