package com.lisory.backend.pedido.dto;

import jakarta.validation.constraints.NotBlank;

public record OrderStatusUpdateRequest(
        @NotBlank String status
) {}
