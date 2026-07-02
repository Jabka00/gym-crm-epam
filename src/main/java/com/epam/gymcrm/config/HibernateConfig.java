package com.epam.gymcrm.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;
import java.util.Properties;

@Slf4j
@Configuration
@EnableTransactionManagement
@RequiredArgsConstructor
public class HibernateConfig {

    private final DatabaseProperties databaseProperties;
    private final HibernateProperties hibernateProperties;

    @Getter
    @Setter
    @Component
    public static class DatabaseProperties {
        @Value("${db.url}")
        private String url;

        @Value("${db.username:root}")
        private String username;

        @Value("${db.password:root}")
        private String password;

        @Value("${db.driver:com.mysql.cj.jdbc.Driver}")
        private String driver;
    }

    @Getter
    @Setter
    @Component
    public static class HibernateProperties {
        @Value("${hibernate.dialect:org.hibernate.dialect.H2Dialect}")
        private String dialect;

        @Value("${hibernate.show_sql:true}")
        private boolean showSql;

        @Value("${hibernate.format_sql:true}")
        private boolean formatSql;

        @Value("${hibernate.hbm2ddl.auto:validate}")
        private String hbm2ddlAuto;

        @Value("${hibernate.jdbc.batch_size:20}")
        private int batchSize;
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer(
            DataSource dataSource,
            @Value("${db.init.enabled:false}") boolean initEnabled) {

        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);

        if (initEnabled) {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("schema.sql"));
            populator.addScript(new ClassPathResource("data.sql"));
            initializer.setDatabasePopulator(populator);
            log.info("Database initialization enabled via classpath SQL scripts");
        } else {
            log.info("Database initialization disabled; expecting schema/data from external source (e.g. Docker)");
        }

        return initializer;
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseProperties.getUrl());
        config.setUsername(databaseProperties.getUsername());
        config.setPassword(databaseProperties.getPassword());
        config.setDriverClassName(databaseProperties.getDriver());

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }

    @Bean
    @DependsOn("dataSourceInitializer")
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan("com.epam.gymcrm.entity");
        sessionFactory.setHibernateProperties(buildHibernateProperties());

        return sessionFactory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalSessionFactoryBean sessionFactory) {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory.getObject());

        return transactionManager;
    }

    private Properties buildHibernateProperties() {
        Properties properties = new Properties();

        properties.put("hibernate.dialect", hibernateProperties.getDialect());

        properties.put("hibernate.show_sql", String.valueOf(hibernateProperties.isShowSql()));
        properties.put("hibernate.format_sql", String.valueOf(hibernateProperties.isFormatSql()));
        properties.put("hibernate.use_sql_comments", "false");

        properties.put("hibernate.hbm2ddl.auto", hibernateProperties.getHbm2ddlAuto());

        properties.put("hibernate.jdbc.batch_size", String.valueOf(hibernateProperties.getBatchSize()));
        properties.put("hibernate.order_inserts", "true");
        properties.put("hibernate.order_updates", "true");
        properties.put("hibernate.jdbc.batch_versioned_data", "true");

        properties.put("hibernate.connection.provider_disables_autocommit", "false");

        properties.put("hibernate.cache.use_second_level_cache", "false");
        properties.put("hibernate.cache.use_query_cache", "false");

        return properties;
    }
}