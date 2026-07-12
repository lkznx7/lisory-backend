package com.lisory.backend.produtos.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CollectionResponse(
        UUID id,
        String name,
        String slug,
        String description,
        Boolean active,
        LocalDateTime createdAt
) {}
