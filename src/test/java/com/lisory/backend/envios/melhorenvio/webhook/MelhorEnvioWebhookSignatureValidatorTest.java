package com.lisory.backend.envios.melhorenvio.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MelhorEnvioWebhookSignatureValidator Tests")
class MelhorEnvioWebhookSignatureValidatorTest {

    private static final String TEST_SECRET = "test-webhook-secret-key-123";
    private MelhorEnvioWebhookSignatureValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MelhorEnvioWebhookSignatureValidator(TEST_SECRET);
    }

    @Test
    @DisplayName("should validate correct HMAC-SHA256 signature")
    void shouldValidateCorrectSignature() throws Exception {
        String payload = "{\"event\":\"order.created\",\"data\":{\"id\":\"123\"}}";

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(TEST_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(key);
        String expectedSignature = HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));

        assertTrue(validator.isValid(payload, expectedSignature));
    }

    @Test
    @DisplayName("should reject invalid signature")
    void shouldRejectInvalidSignature() {
        String payload = "{\"event\":\"order.created\",\"data\":{\"id\":\"123\"}}";
        assertFalse(validator.isValid(payload, "invalid-signature-value"));
    }

    @Test
    @DisplayName("should reject null signature")
    void shouldRejectNullSignature() {
        String payload = "{\"event\":\"order.created\"}";
        assertFalse(validator.isValid(payload, null));
    }

    @Test
    @DisplayName("should reject blank signature")
    void shouldRejectBlankSignature() {
        String payload = "{\"event\":\"order.created\"}";
        assertFalse(validator.isValid(payload, "  "));
    }

    @Test
    @DisplayName("should reject when no secret configured")
    void shouldRejectWhenNoSecretConfigured() {
        MelhorEnvioWebhookSignatureValidator noSecretValidator = new MelhorEnvioWebhookSignatureValidator("");
        assertFalse(noSecretValidator.isValid("payload", "signature"));
    }

    @Test
    @DisplayName("should reject when secret is null")
    void shouldRejectWhenSecretIsNull() {
        MelhorEnvioWebhookSignatureValidator nullSecretValidator = new MelhorEnvioWebhookSignatureValidator(null);
        assertFalse(nullSecretValidator.isValid("payload", "signature"));
    }
}
