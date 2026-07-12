package com.lisory.backend.pedido.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderRequest(
        UUID addressId,
        String couponCode,
        @NotBlank String paymentMethod,
        @NotBlank String guestName,
        @NotBlank @Email String guestEmail,
        @Size(max = 14) String guestCpf,
        @Size(max = 20) String guestPhone,
        @Size(max = 255) String street,
        @Size(max = 10) String number,
        String complement,
        @Size(max = 255) String neighborhood,
        @Size(max = 255) String city,
        @Size(max = 10) String state,
        @Size(max = 8) String zipCode,
        String shippingCarrier,
        String shippingService,
        BigDecimal shippingCost
) {}
