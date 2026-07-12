package com.lisory.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "asaas")
public record AsaasProperties(
    String apiKey,
    String apiUrl,
    String redirectUrl
) {}
