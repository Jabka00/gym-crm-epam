package com.epam.gymcrm.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class HibernateProperties {

    @Value("${hibernate.dialect}")
    private String dialect;

    @Value("${hibernate.show_sql}")
    private boolean showSql;

    @Value("${hibernate.format_sql}")
    private boolean formatSql;

    @Value("${hibernate.hbm2ddl.auto}")
    private String hbm2ddlAuto;
}
