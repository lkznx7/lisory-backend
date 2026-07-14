package com.lisory.backend.envios.melhorenvio.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MelhorEnvioCalculateResponse(
    @JsonProperty("id") int id,
    @JsonProperty("name") String name,
    @JsonProperty("error") String error,
    @JsonProperty("price") String price,
    @JsonProperty("custom_price") String customPrice,
    @JsonProperty("discount") String discount,
    @JsonProperty("currency") String currency,
    @JsonProperty("delivery_time") int deliveryTime,
    @JsonProperty("delivery_range") DeliveryRange deliveryRange,
    @JsonProperty("custom_delivery_time") int customDeliveryTime,
    @JsonProperty("custom_delivery_range") DeliveryRange customDeliveryRange,
    @JsonProperty("packages") List<Package> packages,
    @JsonProperty("additional_services") AdditionalServices additionalServices,
    @JsonProperty("additional") Additional additional,
    @JsonProperty("company") Company company
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Company(
        @JsonProperty("id") int id,
        @JsonProperty("name") String name,
        @JsonProperty("picture") String picture
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DeliveryRange(
        @JsonProperty("min") int min,
        @JsonProperty("max") int max
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Package(
        @JsonProperty("price") String price,
        @JsonProperty("discount") String discount,
        @JsonProperty("format") String format,
        @JsonProperty("weight") String weight,
        @JsonProperty("insurance_value") String insuranceValue,
        @JsonProperty("products") List<PackageProduct> products,
        @JsonProperty("dimensions") Dimensions dimensions
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PackageProduct(
        @JsonProperty("id") String id,
        @JsonProperty("quantity") int quantity
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Dimensions(
        @JsonProperty("height") int height,
        @JsonProperty("width") int width,
        @JsonProperty("length") int length
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdditionalServices(
        @JsonProperty("receipt") boolean receipt,
        @JsonProperty("own_hand") boolean ownHand,
        @JsonProperty("collect") boolean collect
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Additional(
        @JsonProperty("unit") Unit unit
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Unit(
        @JsonProperty("price") int price,
        @JsonProperty("delivery") int delivery
    ) {}
}
