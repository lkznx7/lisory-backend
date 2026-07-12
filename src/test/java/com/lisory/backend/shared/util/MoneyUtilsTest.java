package com.lisory.backend.shared.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MoneyUtils Tests")
class MoneyUtilsTest {

    @Test
    @DisplayName("should convert BigDecimal to cents correctly")
    void shouldConvertToCents() {
        assertEquals(1990L, MoneyUtils.toCents(new BigDecimal("19.90")));
        assertEquals(100L, MoneyUtils.toCents(new BigDecimal("1.00")));
        assertEquals(0L, MoneyUtils.toCents(BigDecimal.ZERO));
        assertEquals(1L, MoneyUtils.toCents(new BigDecimal("0.01")));
    }

    @Test
    @DisplayName("should convert cents to BigDecimal correctly")
    void shouldConvertToBigDecimal() {
        assertEquals(new BigDecimal("19.90"), MoneyUtils.toBigDecimal(1990L));
        assertEquals(new BigDecimal("1.00"), MoneyUtils.toBigDecimal(100L));
        assertEquals(BigDecimal.ZERO, MoneyUtils.toBigDecimal(0L));
        assertEquals(new BigDecimal("0.01"), MoneyUtils.toBigDecimal(1L));
    }

    @Test
    @DisplayName("should handle large values without precision loss")
    void shouldHandleLargeValues() {
        assertEquals(99999999L, MoneyUtils.toCents(new BigDecimal("999999.99")));
        assertEquals(new BigDecimal("999999.99"), MoneyUtils.toBigDecimal(99999999L));
    }
}
