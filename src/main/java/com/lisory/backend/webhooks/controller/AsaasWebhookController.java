package com.lisory.backend.webhooks.controller;

import com.lisory.backend.pagamentos.asaas.dto.AsaasWebhookEvent;
import com.lisory.backend.pagamentos.asaas.webhook.AsaasWebhookService;
import com.lisory.backend.shared.log.StructuredLogger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhooks")
public class AsaasWebhookController {

    private final AsaasWebhookService webhookService;
    private final StructuredLogger logger;

    public AsaasWebhookController(AsaasWebhookService webhookService) {
        this.webhookService = webhookService;
        this.logger = StructuredLogger.forClass(AsaasWebhookController.class);
    }

    @PostMapping("/asaas")
    public ResponseEntity<Void> handleAsaasWebhook(@RequestBody AsaasWebhookEvent event) {
        logger.info("webhook_received", Map.of("gateway", "asaas", "event", event.event()));

        try {
            webhookService.processEvent(event);
            logger.info("webhook_processed", Map.of(
                    "gateway", "asaas",
                    "paymentId", event.payment() != null ? event.payment().id() : "unknown"
            ));
        } catch (Exception e) {
            logger.error("webhook_processing_error", Map.of("gateway", "asaas"), e);
            return ResponseEntity.status(500).build();
        }
        return ResponseEntity.ok().build();
    }
}
