package com.lisory.backend.envios.melhorenvio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MelhorEnvioTrackingResponse(
    String id,
    String protocol,
    String status,
    @JsonProperty("tracking_url") String trackingUrl,
    List<MelhorEnvioTrackingEvent> tracking
) {
    public record MelhorEnvioTrackingEvent(
        String status,
        String description,
        String location,
        String date
    ) {}
}
