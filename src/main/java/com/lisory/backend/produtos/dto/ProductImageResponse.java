package com.lisory.backend.produtos.dto;

import java.util.UUID;

public record ProductImageResponse(
        UUID id,
        String imageUrl,
        Boolean isPrimary
) {}
