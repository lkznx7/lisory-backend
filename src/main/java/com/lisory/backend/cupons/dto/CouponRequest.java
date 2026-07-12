package com.lisory.backend.cupons.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 20) String discountType,
        @NotNull @DecimalMin("0.01") BigDecimal discountValue,
        @DecimalMin("0") BigDecimal minOrderValue,
        @Min(1) Integer maxUses,
        @Min(1) Integer maxUsesPerCustomer,
        @Future LocalDateTime expiresAt,
        Boolean active
) {}
