package com.lisory.backend.pagamentos.entity;

public enum PaymentStatus {
    PENDING,
    PROCESSING,
    APPROVED,
    PAID,
    DECLINED,
    CANCELLED,
    REFUNDED,
    CHARGEBACK,
    EXPIRED
}
