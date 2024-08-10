package com.huyle.ms.saga.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "com.huyle.ms.saga")
@EntityScan(basePackages = "com.huyle.ms.saga.entity")
@EnableJpaRepositories(basePackages = "com.huyle.ms.saga.repository")
public class SagaConfig {
}
