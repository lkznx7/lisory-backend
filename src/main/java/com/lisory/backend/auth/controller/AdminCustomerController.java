package com.lisory.backend.auth.controller;

import com.lisory.backend.auth.entity.AuthEntity;
import com.lisory.backend.auth.entity.ROLES;
import com.lisory.backend.auth.repository.AuthRepository;
import com.lisory.backend.exception.ResourceNotFoundException;
import com.lisory.backend.pedido.entity.Order;
import com.lisory.backend.pedido.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/customers")
public class AdminCustomerController {

    private final AuthRepository authRepository;
    private final OrderRepository orderRepository;

    public AdminCustomerController(AuthRepository authRepository, OrderRepository orderRepository) {
        this.authRepository = authRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Page<AuthEntity> users;
        if (search != null && !search.isBlank()) {
            users = authRepository.findByEmailContainingIgnoreCase(search, PageRequest.of(page, size, Sort.by("email")));
        } else {
            users = authRepository.findByRole(ROLES.USER, PageRequest.of(page, size, Sort.by("email")));
        }

        var result = users.map(user -> {
            var orders = orderRepository.findByUserId(user.getId(), PageRequest.of(0, 1000));
            long orderCount = orders.getTotalElements();
            BigDecimal totalSpent = orders.getContent().stream()
                    .map(Order::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return (Object) Map.of(
                    "id", user.getId().toString(),
                    "name", user.getEmail().split("@")[0],
                    "email", user.getEmail(),
                    "phone", "",
                    "orderCount", orderCount,
                    "totalSpent", totalSpent,
                    "createdAt", user.getId() != null ? user.getId().toString() : ""
            );
        });
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable UUID id) {
        AuthEntity user = authRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        var orders = orderRepository.findByUserId(user.getId(), PageRequest.of(0, 1000));
        long orderCount = orders.getTotalElements();
        BigDecimal totalSpent = orders.getContent().stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return ResponseEntity.ok(Map.of(
                "id", user.getId().toString(),
                "name", user.getEmail().split("@")[0],
                "email", user.getEmail(),
                "phone", "",
                "orderCount", orderCount,
                "totalSpent", totalSpent
        ));
    }

    @GetMapping("/top")
    public ResponseEntity<?> getTopCustomers(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(Map.of("customers", java.util.List.of()));
    }
}
