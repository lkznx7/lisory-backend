package com.lisory.backend.auth.controller;

import com.lisory.backend.auth.entity.AuthEntity;
import com.lisory.backend.auth.services.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody com.lisory.backend.auth.dto.AuthRequest user) {
        authenticationService.register(user);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody com.lisory.backend.auth.dto.AuthRequest dto) {
        return ResponseEntity.ok(authenticationService.authenticate(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthEntity user)) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        return ResponseEntity.ok(Map.of(
                "id", user.getId().toString(),
                "email", user.getEmail(),
                "role", user.getRole() != null ? user.getRole().name() : "USER",
                "isActive", user.getActive()
        ));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthEntity user)) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        authenticationService.updateProfile(user.getId(), request.email());
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthEntity user)) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        authenticationService.changePassword(user.getId(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }

    public record UpdateProfileRequest(
        @jakarta.validation.constraints.NotBlank 
        @jakarta.validation.constraints.Email 
        String email
    ) {}

    public record ChangePasswordRequest(
        @jakarta.validation.constraints.NotBlank 
        String currentPassword,
        
        @jakarta.validation.constraints.NotBlank 
        @jakarta.validation.constraints.Size(min = 8) 
        String newPassword
    ) {}
}
