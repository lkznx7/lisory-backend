package com.lisory.backend.pedido.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID userId,
        String userName,
        String userEmail,
        UUID addressId,
        String addressSummary,
        UUID couponId,
        String couponCode,
        String status,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal shippingCost,
        BigDecimal total,
        String guestName,
        String guestEmail,
        String guestPhone,
        String guestCpf,
        List<OrderItemResponse> items,
        UUID paymentId,
        String paymentStatus,
        String paymentMethod,
        String paymentLink,
        UUID shipmentId,
        String shipmentStatus,
        String trackingCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
