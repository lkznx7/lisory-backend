package com.lisory.backend.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
    LoginRate login,
    RegisterRate register
) {
    public record LoginRate(int maxAttempts, int durationMinutes) {}
    public record RegisterRate(int maxAttempts, int durationMinutes) {}
}
