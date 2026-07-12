package com.lisory.backend.pedido.controller;

import com.lisory.backend.auth.entity.AuthEntity;
import com.lisory.backend.pedido.dto.OrderRequest;
import com.lisory.backend.pedido.dto.OrderResponse;
import com.lisory.backend.pedido.services.OrderFacade;
import com.lisory.backend.pedido.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderFacade orderFacade;

    public OrderController(OrderService orderService, OrderFacade orderFacade) {
        this.orderService = orderService;
        this.orderFacade = orderFacade;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        AuthEntity user = getCurrentUser();
        return ResponseEntity.ok(orderFacade.checkout(user.getId(), null, request));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> findAll(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        AuthEntity user = getCurrentUser();
        return ResponseEntity.ok(orderService.findByUser(user.getId(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable UUID id) {
        AuthEntity user = getCurrentUser();
        OrderResponse order = orderService.findById(id);
        if (order.userId() != null && !order.userId().equals(user.getId())) {
            throw new com.lisory.backend.exception.ResourceNotFoundException("Order", "id", id);
        }
        return ResponseEntity.ok(order);
    }

    private AuthEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthEntity user) {
            return user;
        }
        throw new com.lisory.backend.exception.ResourceNotFoundException("User", "authentication", null);
    }
}
