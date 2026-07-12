package com.lisory.backend.cupons.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CouponResponse(
        UUID id,
        String code,
        String discountType,
        BigDecimal discountValue,
        BigDecimal minOrderValue,
        Integer maxUses,
        Integer usedCount,
        Integer maxUsesPerCustomer,
        LocalDateTime expiresAt,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
