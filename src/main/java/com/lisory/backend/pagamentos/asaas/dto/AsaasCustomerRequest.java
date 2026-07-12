package com.lisory.backend.pagamentos.asaas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AsaasCustomerRequest(
    @JsonProperty("name") String name,
    @JsonProperty("email") String email,
    @JsonProperty("cpfCnpj") String cpfCnpj,
    @JsonProperty("phone") String phone
) {}
