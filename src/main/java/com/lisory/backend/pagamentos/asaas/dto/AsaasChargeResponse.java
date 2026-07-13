package com.lisory.backend.pagamentos.asaas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record AsaasChargeResponse(
    @JsonProperty("id") String id,
    @JsonProperty("status") String status,
    @JsonProperty("invoiceUrl") String invoiceUrl,
    @JsonProperty("invoiceNumber") String invoiceNumber,
    @JsonProperty("value") BigDecimal value,
    @JsonProperty("netValue") BigDecimal netValue,
    @JsonProperty("billingType") String billingType,
    @JsonProperty("externalReference") String externalReference,
    @JsonProperty("transactionReceiptUrl") String transactionReceiptUrl,
    @JsonProperty("qrCode") String qrCode,
    @JsonProperty("pixCopyAndPaste") String pixCopyAndPaste,
    @JsonProperty("paymentDate") String paymentDate,
    @JsonProperty("installment") Integer installment,
    @JsonProperty("installmentValue") BigDecimal installmentValue
) {}
