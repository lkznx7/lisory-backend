package com.lisory.backend.shared.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {

    private static final BigDecimal CENTS_MULTIPLIER = new BigDecimal("100");

    private MoneyUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static long toCents(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Value must not be null");
        }
        return value.multiply(CENTS_MULTIPLIER).setScale(0, RoundingMode.HALF_UP).longValue();
    }

    public static BigDecimal toBigDecimal(Long cents) {
        if (cents == null) {
            throw new IllegalArgumentException("Cents must not be null");
        }
        if (cents == 0L) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(cents).divide(CENTS_MULTIPLIER, 2, RoundingMode.HALF_UP);
    }
}
