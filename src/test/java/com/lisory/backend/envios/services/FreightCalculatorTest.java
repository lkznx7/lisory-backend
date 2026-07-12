package com.lisory.backend.envios.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FreightCalculator Tests")
class FreightCalculatorTest {

    @Mock
    private ShippingProvider shippingProvider;

    @InjectMocks
    private FreightCalculator freightCalculator;

    @Test
    @DisplayName("should calculate freight correctly")
    void shouldCalculateFreight() {
        ShippingRequest expectedRequest = new ShippingRequest(
                "01310100", new BigDecimal("1.500"), 3);
        ShippingQuote expectedQuote = new ShippingQuote(
                "PAC", "PAC", new BigDecimal("25.00"), 7);

        when(shippingProvider.calculate(any(ShippingRequest.class))).thenReturn(expectedQuote);

        List<FreightCalculator.FreightItem> items = List.of(
                new FreightCalculator.FreightItem(new BigDecimal("0.500"), 2),
                new FreightCalculator.FreightItem(new BigDecimal("0.500"), 1)
        );

        ShippingQuote result = freightCalculator.calculate("01310100", items);

        assertNotNull(result);
        assertEquals("PAC", result.carrier());
        assertEquals(new BigDecimal("25.00"), result.cost());
        assertEquals(7, result.estimatedDays());
        verify(shippingProvider, times(1)).calculate(any(ShippingRequest.class));
    }

    @Test
    @DisplayName("should aggregate item weights correctly")
    void shouldAggregateWeights() {
        ShippingQuote quote = new ShippingQuote("SEDEX", "SEDEX", new BigDecimal("35.00"), 3);
        when(shippingProvider.calculate(any())).thenReturn(quote);

        List<FreightCalculator.FreightItem> items = List.of(
                new FreightCalculator.FreightItem(new BigDecimal("0.250"), 4),
                new FreightCalculator.FreightItem(new BigDecimal("0.750"), 2)
        );

        freightCalculator.calculate("22041080", items);

        var capturedRequest = org.mockito.ArgumentCaptor.forClass(ShippingRequest.class);
        verify(shippingProvider).calculate(capturedRequest.capture());

        assertEquals(new BigDecimal("2.500"), capturedRequest.getValue().weight());
        assertEquals(6, capturedRequest.getValue().productCount());
    }
}
