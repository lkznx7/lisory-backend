package com.lisory.backend.envios.melhorenvio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MelhorEnvioCheckoutRequest(
    @JsonProperty("service") int service,
    @JsonProperty("insurance_value") double insuranceValue,
    @JsonProperty("tracking") boolean tracking
) {}
