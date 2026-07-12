package com.lisory.backend.produtos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String slug,
        String description,
        String sku,
        BigDecimal price,
        BigDecimal promotionalPrice,
        Integer stockQuantity,
        UUID categoryId,
        String categoryName,
        UUID collectionId,
        String collectionName,
        Boolean active,
        Boolean featured,
        BigDecimal weight,
        BigDecimal height,
        BigDecimal width,
        BigDecimal length,
        List<ProductImageResponse> images,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
