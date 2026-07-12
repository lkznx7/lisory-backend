package com.lisory.backend.webhooks.controller;

import com.lisory.backend.envios.melhorenvio.dto.MelhorEnvioWebhookEvent;
import com.lisory.backend.envios.melhorenvio.webhook.MelhorEnvioWebhookService;
import com.lisory.backend.envios.melhorenvio.webhook.MelhorEnvioWebhookSignatureValidator;
import com.lisory.backend.shared.log.StructuredLogger;
import com.lisory.backend.shared.util.JsonUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhooks")
public class MelhorEnvioWebhookController {

    private final MelhorEnvioWebhookService webhookService;
    private final MelhorEnvioWebhookSignatureValidator signatureValidator;
    private final StructuredLogger logger;

    public MelhorEnvioWebhookController(
            MelhorEnvioWebhookService webhookService,
            MelhorEnvioWebhookSignatureValidator signatureValidator
    ) {
        this.webhookService = webhookService;
        this.signatureValidator = signatureValidator;
        this.logger = StructuredLogger.forClass(MelhorEnvioWebhookController.class);
    }

    @PostMapping("/melhor-envio")
    public ResponseEntity<Map<String, String>> handleMelhorEnvioWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-ME-Signature", required = false) String signature) {

        logger.info("webhook_received", Map.of("gateway", "melhor-envio"));

        if (!signatureValidator.isValid(payload, signature)) {
            logger.warn("webhook_invalid_signature", Map.of("gateway", "melhor-envio"));
            return ResponseEntity.ok(Map.of("status", "error", "message", "Invalid signature"));
        }

        try {
            MelhorEnvioWebhookEvent event = JsonUtils.fromJson(payload, MelhorEnvioWebhookEvent.class);
            logger.info("webhook_event_parsed", Map.of(
                    "event", event.event() != null ? event.event() : "null"
            ));

            webhookService.processEvent(event);

            logger.info("webhook_acknowledged", Map.of("gateway", "melhor-envio"));
        } catch (Exception e) {
            logger.error("webhook_processing_error", Map.of("gateway", "melhor-envio"), e);
        }
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
