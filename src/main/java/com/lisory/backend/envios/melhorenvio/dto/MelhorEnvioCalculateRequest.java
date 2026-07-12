package com.lisory.backend.envios.melhorenvio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MelhorEnvioCalculateRequest(
    @JsonProperty("from") Address from,
    @JsonProperty("to") Address to,
    @JsonProperty("products") List<Product> products,
    @JsonProperty("options") Options options,
    @JsonProperty("services") String services
) {
    public record Address(
        @JsonProperty("postal_code") String postalCode
    ) {}

    public record Product(
        @JsonProperty("id") String id,
        @JsonProperty("width") double width,
        @JsonProperty("height") double height,
        @JsonProperty("length") double length,
        @JsonProperty("weight") double weight,
        @JsonProperty("insurance_value") double insuranceValue,
        @JsonProperty("quantity") int quantity
    ) {}

    public record Options(
        @JsonProperty("receipt") boolean receipt,
        @JsonProperty("own_hand") boolean ownHand
    ) {}
}
