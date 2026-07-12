package com.lisory.backend.cupons.dto;

public record CouponStatusResponse(
        String code,
        String status,
        String description
) {}
