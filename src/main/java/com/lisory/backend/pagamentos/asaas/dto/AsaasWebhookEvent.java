package com.lisory.backend.pagamentos.asaas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record AsaasWebhookEvent(
    @JsonProperty("event") String event,
    @JsonProperty("payment") PaymentData payment
) {
    public record PaymentData(
        @JsonProperty("id") String id,
        @JsonProperty("status") String status,
        @JsonProperty("externalReference") String externalReference,
        @JsonProperty("value") BigDecimal value,
        @JsonProperty("netValue") BigDecimal netValue,
        @JsonProperty("billingType") String billingType,
        @JsonProperty("invoiceUrl") String invoiceUrl,
        @JsonProperty("invoiceNumber") String invoiceNumber,
        @JsonProperty("transactionReceiptUrl") String transactionReceiptUrl,
        @JsonProperty("qrCode") String qrCode,
        @JsonProperty("pixCopyAndPaste") String pixCopyAndPaste
    ) {}
}
