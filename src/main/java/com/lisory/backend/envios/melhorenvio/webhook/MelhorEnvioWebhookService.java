package com.lisory.backend.envios.melhorenvio.webhook;

import com.lisory.backend.envios.entity.Shipment;
import com.lisory.backend.envios.melhorenvio.dto.MelhorEnvioWebhookEvent;
import com.lisory.backend.envios.repository.ShipmentRepository;
import com.lisory.backend.shared.log.StructuredLogger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class MelhorEnvioWebhookService {

    private static final StructuredLogger log = StructuredLogger.forClass(MelhorEnvioWebhookService.class);

    private final ShipmentRepository shipmentRepository;

    public MelhorEnvioWebhookService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Transactional
    public void processEvent(MelhorEnvioWebhookEvent event) {
        log.info("melhorenvio_webhook_processing", Map.of(
                "event", event.event() != null ? event.event() : "null",
                "dataKeys", event.data() != null ? String.join(",", event.data().keySet()) : "null"
        ));

        if (event.event() == null || event.event().isBlank()) {
            log.warn("melhorenvio_webhook_no_event_type", Map.of());
            return;
        }

        if (event.data() == null) {
            log.warn("melhorenvio_webhook_no_data", Map.of("event", event.event()));
            return;
        }

        String shipmentId = extractShipmentId(event);
        if (shipmentId == null) {
            log.warn("melhorenvio_webhook_no_shipment_id", Map.of("event", event.event()));
            return;
        }

        Shipment shipment = shipmentRepository.findByMelhorEnvioId(shipmentId).orElse(null);
        if (shipment == null) {
            log.warn("melhorenvio_webhook_shipment_not_found", Map.of("melhorEnvioId", shipmentId));
            return;
        }

        String status = mapEventToStatus(event.event());
        if (status != null) {
            shipment.setStatus(status);

            switch (status) {
                case "SHIPPED" -> shipment.setShippedAt(LocalDateTime.now());
                case "DELIVERED" -> shipment.setDeliveredAt(LocalDateTime.now());
                case "CANCELLED" -> shipment.setCancelledAt(LocalDateTime.now());
            }

            if (event.data().containsKey("tracking")) {
                Object trackingObj = event.data().get("tracking");
                if (trackingObj instanceof String tracking) {
                    shipment.setTrackingCode(tracking);
                }
            }

            if (event.data().containsKey("tracking_url")) {
                Object trackingUrlObj = event.data().get("tracking_url");
                if (trackingUrlObj instanceof String trackingUrl) {
                    shipment.setTrackingUrl(trackingUrl);
                }
            }

            shipmentRepository.save(shipment);
            log.info("melhorenvio_webhook_shipment_updated", Map.of(
                    "shipmentId", shipment.getId().toString(),
                    "melhorEnvioId", shipmentId,
                    "newStatus", status
            ));
        } else {
            log.info("melhorenvio_webhook_event_ignored", Map.of("event", event.event()));
        }
    }

    private String extractShipmentId(MelhorEnvioWebhookEvent event) {
        if (event.data().containsKey("id")) {
            Object id = event.data().get("id");
            if (id instanceof String idStr) {
                return idStr;
            }
        }
        if (event.data().containsKey("label_id")) {
            Object labelId = event.data().get("label_id");
            if (labelId instanceof String labelIdStr) {
                return labelIdStr;
            }
        }
        return null;
    }

    private String mapEventToStatus(String event) {
        return switch (event) {
            case "order.created" -> "CREATED";
            case "order.released" -> "RELEASED";
            case "order.generated" -> "GENERATED";
            case "order.received" -> "RECEIVED";
            case "order.posted" -> "SHIPPED";
            case "order.delivered" -> "DELIVERED";
            case "order.cancelled" -> "CANCELLED";
            default -> null;
        };
    }
}
