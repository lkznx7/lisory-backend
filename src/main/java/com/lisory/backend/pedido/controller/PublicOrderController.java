package com.lisory.backend.pedido.controller;

import com.lisory.backend.auth.entity.AuthEntity;
import com.lisory.backend.pedido.dto.OrderRequest;
import com.lisory.backend.pedido.dto.OrderResponse;
import com.lisory.backend.pedido.services.OrderFacade;
import com.lisory.backend.pedido.services.OrderResponseMapper;
import com.lisory.backend.pedido.services.OrderService;
import com.lisory.backend.pedido.entity.Order;
import com.lisory.backend.pedido.repository.OrderRepository;
import com.lisory.backend.exception.ResourceNotFoundException;
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
    private final OrderRepository orderRepository;
    private final OrderResponseMapper responseMapper;

    public PublicOrderController(OrderService orderService, OrderFacade orderFacade,
                                  OrderRepository orderRepository, OrderResponseMapper responseMapper) {
        this.orderService = orderService;
        this.orderFacade = orderFacade;
        this.orderRepository = orderRepository;
        this.responseMapper = responseMapper;
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
    public ResponseEntity<OrderResponse> findById(@PathVariable UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return ResponseEntity.ok(responseMapper.toResponse(order));
    }
}
