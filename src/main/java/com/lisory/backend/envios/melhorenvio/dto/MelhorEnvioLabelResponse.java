package com.lisory.backend.envios.melhorenvio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MelhorEnvioLabelResponse(
    @JsonProperty("id") String id,
    @JsonProperty("protocol") String protocol,
    @JsonProperty("service") String service,
    @JsonProperty("tracking") String tracking,
    @JsonProperty("tracking_url") String trackingUrl,
    @JsonProperty("label") String labelUrl,
    @JsonProperty("status") String status,
    @JsonProperty("insurance_value") String insuranceValue,
    @JsonProperty("delivery_cost") String deliveryCost,
    @JsonProperty("area") String area
) {}
