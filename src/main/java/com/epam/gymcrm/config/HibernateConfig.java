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

    private final DatabaseConfigurationProperties databaseProperties;
    private final HibernateConfigurationProperties hibernateProperties;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseProperties.url());
        config.setUsername(databaseProperties.username());
        config.setPassword(databaseProperties.password());
        config.setDriverClassName(databaseProperties.driver());
        return new HikariDataSource(config);
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan("com.epam.gymcrm.entity");
        sessionFactory.setHibernateProperties(toHibernateProperties());
        return sessionFactory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalSessionFactoryBean sessionFactory) {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory.getObject());
        return transactionManager;
    }

    private Properties toHibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", hibernateProperties.dialect());
        properties.put("hibernate.show_sql", hibernateProperties.showSql());
        properties.put("hibernate.format_sql", hibernateProperties.formatSql());
        properties.put("hibernate.hbm2ddl.auto", hibernateProperties.hbm2ddlAuto());
        return properties;
    }
}
