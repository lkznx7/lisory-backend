package com.lisory.backend.envios.services;

import java.util.List;

public interface ShippingProvider {
    List<ShippingQuote> calculate(ShippingRequest request);
}
