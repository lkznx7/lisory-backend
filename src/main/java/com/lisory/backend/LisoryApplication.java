package com.lisory.backend;

import com.lisory.backend.config.properties.AsaasProperties;
import com.lisory.backend.config.properties.CorsProperties;
import com.lisory.backend.config.properties.DatabaseProperties;
import com.lisory.backend.config.properties.JwtProperties;
import com.lisory.backend.config.properties.MelhorEnvioProperties;
import com.lisory.backend.config.security.RateLimitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
    JwtProperties.class,
    DatabaseProperties.class,
    MelhorEnvioProperties.class,
    AsaasProperties.class,
    CorsProperties.class,
    RateLimitProperties.class
})
public class LisoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(LisoryApplication.class, args);
    }
}
