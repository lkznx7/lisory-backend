package com.lisory.backend.pagamentos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID orderId,
        String paymentMethod,
        BigDecimal amount,
        String status,
        String gatewayId,
        String transactionId,
        String gatewayPaymentId,
        String gatewayOrderId,
        String orderNSU,
        String transactionNSU,
        String paymentLink,
        LocalDateTime expirationDate,
        String authorizationCode,
        Integer installments,
        LocalDateTime paidAt,
        LocalDateTime createdAt
) {}
