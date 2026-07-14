package com.lisory.backend.envios.services;

import java.math.BigDecimal;
import java.util.List;

public record ShippingRequest(
    String zipCode,
    BigDecimal weight,
    Integer productCount,
    List<ProductItem> products
) {
    public record ProductItem(
        String productId,
        int quantity,
        BigDecimal weight,
        BigDecimal width,
        BigDecimal height,
        BigDecimal length
    ) {}
}
