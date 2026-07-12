package com.lisory.backend.pedido.controller;

import com.lisory.backend.pedido.dto.OrderResponse;
import com.lisory.backend.pedido.services.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final OrderService orderService;

    public AdminDashboardController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(orderService.getDashboardStats());
    }

    @GetMapping("/recent-orders")
    public ResponseEntity<Page<OrderResponse>> getRecentOrders(
            @RequestParam(defaultValue = "5") int limit) {
        Page<OrderResponse> orders = orderService.findAll(null, PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(orders);
    }
}
