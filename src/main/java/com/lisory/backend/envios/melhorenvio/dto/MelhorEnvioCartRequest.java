package com.lisory.backend.envios.melhorenvio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MelhorEnvioCartRequest(
    @JsonProperty("service") int service,
    @JsonProperty("from") AddressInfo from,
    @JsonProperty("to") AddressInfo to,
    @JsonProperty("products") List<ProductInfo> products,
    @JsonProperty("volumes") List<VolumeInfo> volumes,
    @JsonProperty("options") CartOptions options
) {
    public record AddressInfo(
        @JsonProperty("name") String name,
        @JsonProperty("phone") String phone,
        @JsonProperty("document") String document,
        @JsonProperty("email") String email,
        @JsonProperty("address") String address,
        @JsonProperty("complement") String complement,
        @JsonProperty("number") String number,
        @JsonProperty("district") String district,
        @JsonProperty("city") String city,
        @JsonProperty("postal_code") String postalCode,
        @JsonProperty("state_abbr") String stateAbbr,
        @JsonProperty("country_id") String countryId,
        @JsonProperty("company_document") String companyDocument,
        @JsonProperty("state_register") String stateRegister,
        @JsonProperty("economic_activity_code") String economicActivityCode
    ) {}

    public record ProductInfo(
        @JsonProperty("name") String name,
        @JsonProperty("quantity") String quantity,
        @JsonProperty("unitary_value") String unitaryValue
    ) {}

    public record VolumeInfo(
        @JsonProperty("height") int height,
        @JsonProperty("width") int width,
        @JsonProperty("length") int length,
        @JsonProperty("weight") int weight
    ) {}

    public record CartOptions(
        @JsonProperty("insurance_value") double insuranceValue,
        @JsonProperty("receipt") boolean receipt,
        @JsonProperty("own_hand") boolean ownHand,
        @JsonProperty("invoice") InvoiceInfo invoice
    ) {}

    public record InvoiceInfo(
        @JsonProperty("key") String key
    ) {}
}
