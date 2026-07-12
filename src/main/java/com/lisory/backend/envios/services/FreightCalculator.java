package com.lisory.backend.envios.services;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public final class FreightCalculator {

    private final ShippingProvider shippingProvider;

    public FreightCalculator(ShippingProvider shippingProvider) {
        this.shippingProvider = shippingProvider;
    }

    public ShippingQuote calculate(String zipCode, List<FreightItem> items) {
        BigDecimal totalWeight = items.stream()
                .map(item -> item.getWeight().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalCount = items.stream()
                .mapToInt(FreightItem::getQuantity)
                .sum();
        ShippingRequest request = new ShippingRequest(zipCode, totalWeight, totalCount);
        return shippingProvider.calculate(request);
    }

    public static class FreightItem {
        private final BigDecimal weight;
        private final int quantity;

        public FreightItem(BigDecimal weight, int quantity) {
            this.weight = weight;
            this.quantity = quantity;
        }

        public BigDecimal getWeight() { return weight; }
        public int getQuantity() { return quantity; }
    }
}
