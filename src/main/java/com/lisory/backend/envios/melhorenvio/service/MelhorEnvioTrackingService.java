package com.lisory.backend.envios.melhorenvio.service;

import com.lisory.backend.envios.melhorenvio.client.MelhorEnvioClient;
import com.lisory.backend.envios.melhorenvio.dto.MelhorEnvioTrackingResponse;
import com.lisory.backend.shared.log.StructuredLogger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MelhorEnvioTrackingService {

    private static final StructuredLogger log = StructuredLogger.forClass(MelhorEnvioTrackingService.class);

    private final MelhorEnvioClient client;

    public MelhorEnvioTrackingService(MelhorEnvioClient client) {
        this.client = client;
    }

    public MelhorEnvioTrackingResponse getTracking(String trackingId) {
        log.info("tracking_get", Map.of("trackingId", trackingId));
        MelhorEnvioTrackingResponse response = client.getTracking(trackingId);

        if (response != null) {
            log.info("tracking_result", Map.of(
                    "trackingId", trackingId,
                    "status", response.status() != null ? response.status() : "UNKNOWN",
                    "events", response.tracking() != null ? String.valueOf(response.tracking().size()) : "0"
            ));
        }

        return response;
    }

    public String getLatestStatus(String trackingId) {
        MelhorEnvioTrackingResponse response = getTracking(trackingId);
        if (response == null) {
            return "UNKNOWN";
        }
        return response.status() != null ? response.status() : "UNKNOWN";
    }

    public String getTrackingUrl(String trackingId) {
        MelhorEnvioTrackingResponse response = getTracking(trackingId);
        if (response == null) {
            return null;
        }
        return response.trackingUrl();
    }

    public List<MelhorEnvioTrackingResponse.MelhorEnvioTrackingEvent> getTrackingEvents(String trackingId) {
        MelhorEnvioTrackingResponse response = getTracking(trackingId);
        if (response == null || response.tracking() == null) {
            return List.of();
        }
        return response.tracking();
    }

    public Map<String, Object> getTrackingSummary(String trackingId) {
        MelhorEnvioTrackingResponse response = getTracking(trackingId);
        if (response == null) {
            return Map.of(
                    "trackingId", trackingId,
                    "status", "UNKNOWN",
                    "events", List.of()
            );
        }

        List<Map<String, String>> events = response.tracking() != null
                ? response.tracking().stream()
                    .map(e -> Map.of(
                            "status", e.status() != null ? e.status() : "",
                            "description", e.description() != null ? e.description() : "",
                            "location", e.location() != null ? e.location() : "",
                            "date", e.date() != null ? e.date() : ""
                    ))
                    .toList()
                : List.of();

        return Map.of(
                "trackingId", response.id() != null ? response.id() : trackingId,
                "protocol", response.protocol() != null ? response.protocol() : "",
                "status", response.status() != null ? response.status() : "UNKNOWN",
                "trackingUrl", response.trackingUrl() != null ? response.trackingUrl() : "",
                "events", events
        );
    }
}
