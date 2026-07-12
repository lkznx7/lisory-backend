package com.lisory.backend.pagamentos.asaas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AsaasCustomerListResponse(
        @JsonProperty("data") List<AsaasCustomerResponse> data
) {}
