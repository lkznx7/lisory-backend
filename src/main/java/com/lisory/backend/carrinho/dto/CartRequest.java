package com.lisory.backend.carrinho.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CartRequest(
        @NotBlank String productId,
        @Min(1) Integer quantity
) {}
