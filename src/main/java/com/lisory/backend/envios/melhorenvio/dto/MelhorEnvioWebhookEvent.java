package com.lisory.backend.envios.melhorenvio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record MelhorEnvioWebhookEvent(
    @JsonProperty("event") String event,
    @JsonProperty("data") Map<String, Object> data
) {}
