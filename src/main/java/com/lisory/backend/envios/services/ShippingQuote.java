package com.lisory.backend.envios.services;

import java.math.BigDecimal;

public record ShippingQuote(
    String carrier,
    String service,
    BigDecimal cost,
    Integer estimatedDays
) {}
