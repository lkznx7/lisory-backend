package com.lisory.backend.envios.services;

import java.math.BigDecimal;

public record ShippingRequest(
    String zipCode,
    BigDecimal weight,
    Integer productCount
) {}
