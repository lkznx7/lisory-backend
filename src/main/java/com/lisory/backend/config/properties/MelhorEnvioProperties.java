package com.lisory.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "melhor-envio")
public record MelhorEnvioProperties(
    String clientId,
    String clientSecret,
    String apiUrl,
    String userAgent,
    String callbackUrl,
    String scopes,
    String originCep
) {}
