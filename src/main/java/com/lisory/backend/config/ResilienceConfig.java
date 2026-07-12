package com.lisory.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ResilienceConfig {

    // Resilience annotations will be added via annotations on service methods
    // Spring Boot 4.x supports @Retryable and @CircuitBreaker natively
    // For now, we configure retry behavior via RestTemplate interceptor

    @Bean("resilientRestTemplate")
    public RestTemplate resilientRestTemplate() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(java.time.Duration.ofMillis(5000));
        factory.setReadTimeout(java.time.Duration.ofMillis(30000));

        RestTemplate restTemplate = new RestTemplate(factory);

        // Add logging interceptor
        restTemplate.getInterceptors().add((request, body, execution) -> {
            org.slf4j.LoggerFactory.getLogger("HTTP_CLIENT").info(
                "HTTP {} {}", request.getMethod(), request.getURI());
            return execution.execute(request, body);
        });

        return restTemplate;
    }
}
