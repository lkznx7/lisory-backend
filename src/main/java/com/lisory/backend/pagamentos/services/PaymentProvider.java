package com.lisory.backend.pagamentos.services;

public interface PaymentProvider {
    GatewayResponse processPayment(PaymentRequest request);
}
