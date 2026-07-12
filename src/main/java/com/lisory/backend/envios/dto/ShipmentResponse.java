package com.lisory.backend.envios.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentResponse(
        UUID id,
        UUID orderId,
        String carrier,
        String service,
        String trackingCode,
        BigDecimal shippingCost,
        String status,
        String labelUrl,
        String trackingUrl,
        String melhorEnvioId,
        String carrierCode,
        String serviceCode,
        LocalDateTime estimatedDelivery,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt
) {}
