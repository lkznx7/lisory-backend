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

    public List<ShippingQuote> calculate(String zipCode, List<FreightItem> items) {
        BigDecimal totalWeight = items.stream()
                .map(item -> item.getWeight().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalCount = items.stream()
                .mapToInt(FreightItem::getQuantity)
                .sum();

        List<ShippingRequest.ProductItem> productItems = items.stream()
                .map(item -> new ShippingRequest.ProductItem(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getWeight(),
                        item.getWidth(),
                        item.getHeight(),
                        item.getLength()
                ))
                .toList();

        ShippingRequest request = new ShippingRequest(zipCode, totalWeight, totalCount, productItems);
        return shippingProvider.calculate(request);
    }

    public static class FreightItem {
        private final String productId;
        private final BigDecimal weight;
        private final int quantity;
        private final BigDecimal width;
        private final BigDecimal height;
        private final BigDecimal length;

        public FreightItem(String productId, BigDecimal weight, int quantity,
                           BigDecimal width, BigDecimal height, BigDecimal length) {
            this.productId = productId;
            this.weight = weight;
            this.quantity = quantity;
            this.width = width;
            this.height = height;
            this.length = length;
        }

        public String getProductId() { return productId; }
        public BigDecimal getWeight() { return weight; }
        public int getQuantity() { return quantity; }
        public BigDecimal getWidth() { return width; }
        public BigDecimal getHeight() { return height; }
        public BigDecimal getLength() { return length; }
    }
}
