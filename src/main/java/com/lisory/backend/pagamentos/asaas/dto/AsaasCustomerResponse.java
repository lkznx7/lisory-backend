package com.lisory.backend.pagamentos.asaas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AsaasCustomerResponse(
    @JsonProperty("id") String id,
    @JsonProperty("name") String name,
    @JsonProperty("email") String email,
    @JsonProperty("cpfCnpj") String cpfCnpj
) {}
