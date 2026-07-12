package com.lisory.backend.envios.melhorenvio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MelhorEnvioTokenResponse(
    String accessToken,
    String tokenType,
    @JsonProperty("expires_in") int expiresIn,
    String scope,
    @JsonProperty("refresh_token") String refreshToken
) {}
