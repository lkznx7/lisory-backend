package com.lisory.backend.pagamentos.asaas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AsaasChargeResponse(
    @JsonProperty("id") String id,
    @JsonProperty("status") String status,
    @JsonProperty("invoiceUrl") String invoiceUrl,
    @JsonProperty("invoiceNumber") String invoiceNumber,
    @JsonProperty("value") Double value,
    @JsonProperty("netValue") Double netValue,
    @JsonProperty("billingType") String billingType,
    @JsonProperty("externalReference") String externalReference
) {}
