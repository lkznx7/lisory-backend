package com.lisory.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.datasource")
public record DatabaseProperties(String url, String username, String password, String driverClassName) {}
