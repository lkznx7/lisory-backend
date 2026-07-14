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
    String originCep,
    String storeName,
    String storePhone,
    String storeDocument,
    String storeEmail,
    String storeAddress,
    String storeNumber,
    String storeComplement,
    String storeNeighborhood,
    String storeCity,
    String storeState,
    String storeStateRegister,
    String storeCnae
) {}
