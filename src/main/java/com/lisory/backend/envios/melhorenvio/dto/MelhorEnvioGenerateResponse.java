package com.lisory.backend.envios.melhorenvio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MelhorEnvioGenerateResponse(
    @JsonProperty("url") String url
) {}
