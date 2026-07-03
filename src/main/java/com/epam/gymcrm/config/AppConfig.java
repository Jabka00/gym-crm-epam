package com.epam.gymcrm.config;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.security.SecureRandom;

@Configuration
@ComponentScan(basePackages = "com.epam.gymcrm")
@PropertySource("classpath:application.properties")
@Import({HibernateConfig.class, ValidationConfig.class})

public class AppConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
}
