package com.lisory.backend.envios.melhorenvio.config;

import com.lisory.backend.config.properties.MelhorEnvioProperties;
import com.lisory.backend.envios.melhorenvio.service.MelhorEnvioOAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class MelhorEnvioConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MelhorEnvioConfiguration.class);

    private final MelhorEnvioOAuthService oAuthService;
    private final MelhorEnvioProperties properties;

    public MelhorEnvioConfiguration(
            MelhorEnvioOAuthService oAuthService,
            MelhorEnvioProperties properties
    ) {
        this.oAuthService = oAuthService;
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Melhor Envio configuration initialized");
        log.info("API URL: {}", properties.apiUrl());
        log.info("Client ID: {}", properties.clientId());
        log.info("Has stored tokens: {}", oAuthService.hasTokens());
        log.info("Token valid: {}", oAuthService.isTokenValid());
    }
}
