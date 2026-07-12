package com.lisory.backend.envios.melhorenvio.webhook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class MelhorEnvioWebhookSignatureValidator {

    private static final Logger log = LoggerFactory.getLogger(MelhorEnvioWebhookSignatureValidator.class);
    private static final String HMAC_SHA256 = "HmacSHA256";

    private final String clientSecret;

    public MelhorEnvioWebhookSignatureValidator(
            @Value("${melhor-envio.client-secret:}") String clientSecret
    ) {
        this.clientSecret = clientSecret;
    }

    public boolean isValid(String payload, String signature) {
        if (clientSecret == null || clientSecret.isBlank()) {
            log.warn("melhor_envio_webhook_no_secret_configured");
            return false;
        }

        if (signature == null || signature.isBlank()) {
            log.warn("melhor_envio_webhook_missing_signature");
            return false;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKey = new SecretKeySpec(
                    clientSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256
            );
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = HexFormat.of().formatHex(hash);

            boolean valid = MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );
            if (!valid) {
                log.warn("melhor_envio_webhook_invalid_signature");
            }
            return valid;
        } catch (Exception e) {
            log.error("melhor_envio_webhook_signature_validation_error", e);
            return false;
        }
    }
}
