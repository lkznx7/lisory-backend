package com.lisory.backend.auth.services;

import com.lisory.backend.auth.dto.AuthRequest;
import com.lisory.backend.auth.dto.AuthResponse;
import com.lisory.backend.auth.entity.AuthEntity;
import com.lisory.backend.auth.exception.EmailAlreadyExistsException;
import com.lisory.backend.auth.exception.InvalidCredentialsException;
import com.lisory.backend.auth.repository.AuthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthRepository authRepository;
    @Mock
    private TokenProvider tokenProvider;

    private PasswordEncoder passwordEncoder;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authenticationService = new AuthenticationService(
                authRepository, passwordEncoder, tokenProvider
        );
    }

    @Test
    void register_shouldCreateUser_whenEmailNotExists() {
        AuthRequest request = new AuthRequest("test@example.com", "password123");
        when(authRepository.existsByEmail("test@example.com")).thenReturn(false);

        authenticationService.register(request);

        verify(authRepository).save(any(AuthEntity.class));
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        AuthRequest request = new AuthRequest("existing@example.com", "password123");
        when(authRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authenticationService.register(request));
        verify(authRepository, never()).save(any());
    }

    @Test
    void authenticate_shouldReturnToken_whenCredentialsValid() {
        AuthRequest request = new AuthRequest("test@example.com", "password123");
        AuthEntity entity = new AuthEntity("test@example.com", passwordEncoder.encode("password123"));
        entity.setId(UUID.randomUUID());

        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.of(entity));

        when(tokenProvider.generateToken("test@example.com")).thenReturn("jwt-token");

        AuthResponse response = authenticationService.authenticate(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
    }

    @Test
    void authenticate_shouldThrow_whenWrongPassword() {
        AuthRequest request = new AuthRequest("test@example.com", "wrongpassword");
        AuthEntity entity = new AuthEntity("test@example.com", passwordEncoder.encode("correctpassword"));

        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.of(entity));

        assertThrows(InvalidCredentialsException.class, () -> authenticationService.authenticate(request));
    }

    @Test
    void authenticate_shouldThrow_whenEmailNotFound() {
        AuthRequest request = new AuthRequest("unknown@example.com", "password123");

        when(authRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authenticationService.authenticate(request));
    }
}
