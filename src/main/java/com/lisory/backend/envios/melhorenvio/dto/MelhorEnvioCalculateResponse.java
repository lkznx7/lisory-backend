package com.lisory.backend.envios.melhorenvio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record MelhorEnvioCalculateResponse(
    @JsonProperty("options") List<ShippingOption> options
) {
    public record ShippingOption(
        @JsonProperty("id") int id,
        @JsonProperty("company") Company company,
        @JsonProperty("name") String name,
        @JsonProperty("error") String error,
        @JsonProperty("price") String price,
        @JsonProperty("discount") String discount,
        @JsonProperty("delivery_max") String deliveryMax,
        @JsonProperty("delivery_min") String deliveryMin,
        @JsonProperty("custom_delivery") String customDelivery,
        @JsonProperty("custom_delivery_max") String customDeliveryMax,
        @JsonProperty("original_delivery_min") String originalDeliveryMin,
        @JsonProperty("original_delivery_max") String originalDeliveryMax,
        @JsonProperty("days") String days,
        @JsonProperty("delivery_range") DeliveryRange deliveryRange,
        @JsonProperty("only_receiver") String onlyReceiver,
        @JsonProperty("tracking") String tracking,
        @JsonProperty("pickup") boolean pickup
    ) {
        public record Company(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("picture") String picture
        ) {}

        public record DeliveryRange(
            @JsonProperty("min") int min,
            @JsonProperty("max") int max
        ) {}
    }
}
