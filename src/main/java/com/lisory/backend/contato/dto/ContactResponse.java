package com.lisory.backend.contato.dto;

import java.time.LocalDateTime;

public record ContactResponse(
    String message,
    LocalDateTime receivedAt
) {}
