package com.lisory.backend.shared.util;

import java.util.Objects;

public final class CepValidator {

    private static final String CEP_REGEX = "^\\d{8}$";

    private CepValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static boolean isValid(String cep) {
        if (Objects.isNull(cep)) {
            return false;
        }
        return cep.matches(CEP_REGEX);
    }

    public static String format(String cep) {
        String cleaned = clean(cep);
        if (!cleaned.matches(CEP_REGEX)) {
            throw new IllegalArgumentException("Invalid CEP: " + cep);
        }
        return cleaned.substring(0, 5) + "-" + cleaned.substring(5);
    }

    public static String clean(String cep) {
        if (Objects.isNull(cep)) {
            return "";
        }
        return cep.replaceAll("\\D", "");
    }
}
