package com.lisory.backend.envios.melhorenvio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MelhorEnvioCheckoutRequest(
    @JsonProperty("orders") List<String> orders
) {}
