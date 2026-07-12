package com.lisory.backend.pagamentos.asaas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AsaasWebhookEvent(
    @JsonProperty("event") String event,
    @JsonProperty("payment") PaymentData payment
) {
    public record PaymentData(
        @JsonProperty("id") String id,
        @JsonProperty("status") String status,
        @JsonProperty("externalReference") String externalReference,
        @JsonProperty("value") Double value,
        @JsonProperty("netValue") Double netValue,
        @JsonProperty("billingType") String billingType,
        @JsonProperty("invoiceUrl") String invoiceUrl,
        @JsonProperty("invoiceNumber") String invoiceNumber
    ) {}
}
