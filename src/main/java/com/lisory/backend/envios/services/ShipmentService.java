package com.lisory.backend.envios.services;

import com.lisory.backend.envios.dto.ShipmentResponse;
import com.lisory.backend.envios.entity.Shipment;
import com.lisory.backend.envios.repository.ShipmentRepository;
import com.lisory.backend.exception.ResourceNotFoundException;
import com.lisory.backend.pedido.entity.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;

    public ShipmentService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    public ShipmentResponse createShipment(UUID orderId, ShippingQuote quote) {
        Shipment shipment = new Shipment();
        Order orderStub = new Order();
        orderStub.setId(orderId);
        shipment.setOrder(orderStub);
        shipment.setCarrier(quote.carrier());
        shipment.setService(quote.service());
        shipment.setShippingCost(quote.cost());
        shipment.setStatus("PENDING");

        Shipment saved = shipmentRepository.save(shipment);
        return toResponse(saved);
    }

    @Transactional
    public ShipmentResponse updateTracking(UUID orderId, String trackingCode, String carrier, String service) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "orderId", orderId));
        shipment.setTrackingCode(trackingCode);
        shipment.setCarrier(carrier);
        shipment.setService(service);

        Shipment saved = shipmentRepository.save(shipment);
        return toResponse(saved);
    }

    public ShipmentResponse updateStatus(UUID orderId, String status) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "orderId", orderId));
        shipment.setStatus(status);

        if ("SHIPPED".equals(status)) {
            shipment.setShippedAt(LocalDateTime.now());
        } else if ("DELIVERED".equals(status)) {
            shipment.setDeliveredAt(LocalDateTime.now());
        }

        Shipment saved = shipmentRepository.save(shipment);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse findByOrderId(UUID orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "orderId", orderId));
        return toResponse(shipment);
    }

    private ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getOrder().getId(),
                shipment.getCarrier(),
                shipment.getService(),
                shipment.getTrackingCode(),
                shipment.getShippingCost(),
                shipment.getStatus(),
                shipment.getLabelUrl(),
                shipment.getTrackingUrl(),
                shipment.getMelhorEnvioId(),
                shipment.getCarrierCode(),
                shipment.getServiceCode(),
                shipment.getEstimatedDelivery(),
                shipment.getShippedAt(),
                shipment.getDeliveredAt(),
                shipment.getCreatedAt()
        );
    }
}
