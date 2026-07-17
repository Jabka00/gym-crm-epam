package com.epam.gymcrm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record HibernateConfigurationProperties(
        @Value("${hibernate.dialect}") String dialect,
        @Value("${hibernate.show_sql}") boolean showSql,
        @Value("${hibernate.format_sql}") boolean formatSql,
        @Value("${hibernate.hbm2ddl.auto}") String hbm2ddlAuto
) {
}
