package com.lisory.backend.carrinho.controller;

import com.lisory.backend.auth.entity.AuthEntity;
import com.lisory.backend.carrinho.dto.CartRequest;
import com.lisory.backend.carrinho.dto.CartResponse;
import com.lisory.backend.carrinho.dto.CartUpdateRequest;
import com.lisory.backend.carrinho.services.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader(value = "X-Guest-Cart-Id", required = false) UUID guestCartId) {
        UUID userId = getCurrentUserId();
        return ResponseEntity.ok(cartService.getCart(userId, guestCartId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @Valid @RequestBody CartRequest request,
            @RequestHeader(value = "X-Guest-Cart-Id", required = false) UUID guestCartId) {
        UUID userId = getCurrentUserId();
        return ResponseEntity.ok(cartService.addItem(userId, guestCartId, request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            @PathVariable UUID itemId,
            @Valid @RequestBody CartUpdateRequest request,
            @RequestHeader(value = "X-Guest-Cart-Id", required = false) UUID guestCartId) {
        UUID userId = getCurrentUserId();
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, guestCartId, itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable UUID itemId,
            @RequestHeader(value = "X-Guest-Cart-Id", required = false) UUID guestCartId) {
        UUID userId = getCurrentUserId();
        cartService.removeItem(userId, guestCartId, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestHeader(value = "X-Guest-Cart-Id", required = false) UUID guestCartId) {
        UUID userId = getCurrentUserId();
        cartService.clearCart(userId, guestCartId);
        return ResponseEntity.noContent().build();
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthEntity user) {
            return user.getId();
        }
        return null;
    }
}
