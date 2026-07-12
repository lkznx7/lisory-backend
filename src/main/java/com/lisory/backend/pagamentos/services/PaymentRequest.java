package com.lisory.backend.pagamentos.services;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
    UUID orderId,
    BigDecimal amount,
    String paymentMethod
) {}
