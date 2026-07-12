package com.lisory.backend.carrinho.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID productId,
        String productName,
        String productImage,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal total
) {}
