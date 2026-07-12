package com.lisory.backend.shared.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CepValidator Tests")
class CepValidatorTest {

    @Test
    @DisplayName("should validate correct CEP format")
    void shouldValidateCorrectCep() {
        assertTrue(CepValidator.isValid("01310100"));
        assertTrue(CepValidator.isValid("22041080"));
        assertTrue(CepValidator.isValid("30130000"));
    }

    @Test
    @DisplayName("should reject invalid CEP formats")
    void shouldRejectInvalidCep() {
        assertFalse(CepValidator.isValid(null));
        assertFalse(CepValidator.isValid(""));
        assertFalse(CepValidator.isValid("1234567"));   // 7 digits
        assertFalse(CepValidator.isValid("123456789")); // 9 digits
        assertFalse(CepValidator.isValid("1234567a"));  // letter
        assertFalse(CepValidator.isValid("12.345-678")); // formatted
    }

    @Test
    @DisplayName("should clean CEP correctly")
    void shouldCleanCep() {
        assertEquals("01310100", CepValidator.clean("01310-100"));
        assertEquals("01310100", CepValidator.clean("01310100"));
        assertEquals("01310100", CepValidator.clean("013.101-00"));
    }

    @Test
    @DisplayName("should format CEP correctly")
    void shouldFormatCep() {
        assertEquals("01310-100", CepValidator.format("01310100"));
        assertEquals("22041-080", CepValidator.format("22041080"));
    }
}
