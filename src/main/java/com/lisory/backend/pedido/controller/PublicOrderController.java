package com.lisory.backend.pedido.controller;

import com.lisory.backend.auth.entity.AuthEntity;
import com.lisory.backend.pedido.dto.OrderRequest;
import com.lisory.backend.pedido.dto.OrderResponse;
import com.lisory.backend.pedido.services.OrderFacade;
import com.lisory.backend.pedido.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/orders/public")
public class PublicOrderController {

    private final OrderService orderService;
    private final OrderFacade orderFacade;

    public PublicOrderController(OrderService orderService, OrderFacade orderFacade) {
        this.orderService = orderService;
        this.orderFacade = orderFacade;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createGuestOrder(
            @Valid @RequestBody OrderRequest request,
            @RequestHeader(value = "X-Guest-Cart-Id", required = false) UUID guestCartId) {
        UUID userId = getCurrentUserId();
        return ResponseEntity.ok(orderFacade.checkout(userId, guestCartId, request));
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthEntity user) {
            return user.getId();
        }
        return null;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable UUID id) {
        return ResponseEntity.status(404).body(Map.of("error", "Endpoint not available"));
    }
}
