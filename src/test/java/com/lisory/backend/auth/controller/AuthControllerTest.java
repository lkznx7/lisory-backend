package com.lisory.backend.auth.controller;

import com.lisory.backend.auth.dto.AuthRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthRequestValidationTest {

    @Test
    void authRequest_shouldRejectNullEmail() {
        jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory();
        jakarta.validation.Validator validator = factory.getValidator();

        AuthRequest request = new AuthRequest(null, "password123");
        var violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void authRequest_shouldRejectBlankEmail() {
        jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory();
        jakarta.validation.Validator validator = factory.getValidator();

        AuthRequest request = new AuthRequest("", "password123");
        var violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void authRequest_shouldRejectInvalidEmail() {
        jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory();
        jakarta.validation.Validator validator = factory.getValidator();

        AuthRequest request = new AuthRequest("invalid", "password123");
        var violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void authRequest_shouldRejectNullPassword() {
        jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory();
        jakarta.validation.Validator validator = factory.getValidator();

        AuthRequest request = new AuthRequest("test@example.com", null);
        var violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void authRequest_shouldRejectShortPassword() {
        jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory();
        jakarta.validation.Validator validator = factory.getValidator();

        AuthRequest request = new AuthRequest("test@example.com", "short");
        var violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void authRequest_shouldAcceptValidInput() {
        jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory();
        jakarta.validation.Validator validator = factory.getValidator();

        AuthRequest request = new AuthRequest("test@example.com", "password123");
        var violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void globalExceptionHandler_shouldHandleExceptions() {
        com.lisory.backend.auth.exception.GlobalExceptionHandler handler =
                new com.lisory.backend.auth.exception.GlobalExceptionHandler();
        var response = handler.handleBadRequest(new IllegalArgumentException("test error"));
        assertEquals(400, response.getStatusCode().value());
    }
}
