package com.lisory.backend.pagamentos.asaas.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AsaasChargeRequest(
    @JsonProperty("customer") String customerId,
    @JsonProperty("billingType") String billingType,
    @JsonProperty("value") BigDecimal value,
    @JsonProperty("dueDate") String dueDate,
    @JsonProperty("description") String description,
    @JsonProperty("externalReference") String externalReference,
    @JsonProperty("redirectUrl") String redirectUrl,
    @JsonProperty("installment") Integer installment,
    @JsonProperty("installmentValue") BigDecimal installmentValue
) {}
