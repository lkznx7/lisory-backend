package com.lisory.backend.envios.melhorenvio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MelhorEnvioPrintRequest(
    @JsonProperty("orders") List<String> orders,
    @JsonProperty("mode") String mode
) {}
