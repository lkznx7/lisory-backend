package com.lisory.backend.user.controller;

import com.lisory.backend.auth.entity.AuthEntity;
import com.lisory.backend.user.dto.AddressRequest;
import com.lisory.backend.user.dto.AddressResponse;
import com.lisory.backend.user.services.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    public ResponseEntity<AddressResponse> create(@Valid @RequestBody AddressRequest request) {
        AuthEntity user = getCurrentUser();
        return ResponseEntity.ok(addressService.create(user.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> findAll() {
        AuthEntity user = getCurrentUser();
        return ResponseEntity.ok(addressService.findByUserId(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> findById(@PathVariable UUID id) {
        AuthEntity user = getCurrentUser();
        return ResponseEntity.ok(addressService.findById(user.getId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> update(@PathVariable UUID id, @Valid @RequestBody AddressRequest request) {
        AuthEntity user = getCurrentUser();
        return ResponseEntity.ok(addressService.update(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        AuthEntity user = getCurrentUser();
        addressService.delete(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    private AuthEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthEntity user) {
            return user;
        }
        throw new com.lisory.backend.exception.ResourceNotFoundException("User", "authentication", null);
    }
}
