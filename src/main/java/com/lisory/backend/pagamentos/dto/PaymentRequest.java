package com.lisory.backend.pagamentos.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentRequest(
        @NotBlank String paymentMethod
) {}
