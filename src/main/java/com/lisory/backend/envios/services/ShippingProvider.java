package com.lisory.backend.envios.services;

public interface ShippingProvider {
    ShippingQuote calculate(ShippingRequest request);
}
