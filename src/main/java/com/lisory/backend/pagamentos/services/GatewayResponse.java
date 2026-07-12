package com.lisory.backend.pagamentos.services;

public record GatewayResponse(
    String gatewayId,
    String transactionId,
    String status,
    String paymentUrl,
    String slug
) {
    public GatewayResponse(String gatewayId, String transactionId, String status, String paymentUrl) {
        this(gatewayId, transactionId, status, paymentUrl, null);
    }
}
