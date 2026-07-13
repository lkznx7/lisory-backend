package com.lisory.backend.pagamentos.services;

public record GatewayResponse(
    String gatewayId,
    String transactionId,
    String status,
    String paymentUrl,
    String slug,
    String qrCode,
    String pixCopyAndPaste,
    String transactionReceiptUrl
) {
    public GatewayResponse(String gatewayId, String transactionId, String status, String paymentUrl) {
        this(gatewayId, transactionId, status, paymentUrl, null, null, null, null);
    }

    public GatewayResponse(String gatewayId, String transactionId, String status,
                           String paymentUrl, String slug) {
        this(gatewayId, transactionId, status, paymentUrl, slug, null, null, null);
    }
}
