package com.lisory.backend.envios.controller;

import com.lisory.backend.envios.dto.ShipmentResponse;
import com.lisory.backend.envios.services.ShipmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ShipmentResponse> getByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(shipmentService.findByOrderId(orderId));
    }

    @PutMapping("/order/{orderId}/tracking")
    public ResponseEntity<ShipmentResponse> updateTracking(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateTrackingRequest request) {
        return ResponseEntity.ok(
                shipmentService.updateTracking(orderId, request.trackingCode(), request.carrier(), request.service()));
    }

    @PutMapping("/order/{orderId}/status")
    public ResponseEntity<ShipmentResponse> updateStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(shipmentService.updateStatus(orderId, request.status()));
    }

    record UpdateTrackingRequest(String trackingCode, String carrier, String service) {}
    record UpdateStatusRequest(@NotBlank String status) {}
}
