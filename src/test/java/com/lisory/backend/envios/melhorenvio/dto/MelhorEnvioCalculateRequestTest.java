package com.lisory.backend.envios.melhorenvio.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MelhorEnvioCalculateRequest Tests")
class MelhorEnvioCalculateRequestTest {

    @Test
    @DisplayName("should create calculate request with correct structure")
    void shouldCreateCalculateRequest() {
        MelhorEnvioCalculateRequest request = new MelhorEnvioCalculateRequest(
                new MelhorEnvioCalculateRequest.Address("01310100"),
                new MelhorEnvioCalculateRequest.Address("22041080"),
                List.of(
                        new MelhorEnvioCalculateRequest.Product(
                                "Product A", 15.0, 10.0, 20.0, 0.5, 50.0, 1
                        )
                ),
                new MelhorEnvioCalculateRequest.Options(false, false),
                null
        );

        assertEquals("01310100", request.from().postalCode());
        assertEquals("22041080", request.to().postalCode());
        assertEquals(1, request.products().size());
        assertEquals("Product A", request.products().get(0).id());
        assertFalse(request.options().receipt());
    }

    @Test
    @DisplayName("should create calculate request with services filter")
    void shouldCreateRequestWithServices() {
        MelhorEnvioCalculateRequest request = new MelhorEnvioCalculateRequest(
                new MelhorEnvioCalculateRequest.Address("01310100"),
                new MelhorEnvioCalculateRequest.Address("22041080"),
                List.of(),
                new MelhorEnvioCalculateRequest.Options(false, false),
                "1,2,18"
        );

        assertEquals("1,2,18", request.services());
    }
}
