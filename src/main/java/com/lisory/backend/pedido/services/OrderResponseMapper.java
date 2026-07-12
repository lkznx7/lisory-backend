package com.lisory.backend.pedido.services;

import com.lisory.backend.envios.entity.Shipment;
import com.lisory.backend.envios.repository.ShipmentRepository;
import com.lisory.backend.pagamentos.entity.Payment;
import com.lisory.backend.pagamentos.repository.PaymentRepository;
import com.lisory.backend.pedido.dto.OrderItemResponse;
import com.lisory.backend.pedido.dto.OrderResponse;
import com.lisory.backend.pedido.entity.Order;
import com.lisory.backend.produtos.entity.ProductImage;
import com.lisory.backend.user.entity.Address;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderResponseMapper {

    private final PaymentRepository paymentRepository;
    private final ShipmentRepository shipmentRepository;

    public OrderResponseMapper(PaymentRepository paymentRepository, ShipmentRepository shipmentRepository) {
        this.paymentRepository = paymentRepository;
        this.shipmentRepository = shipmentRepository;
    }

    public OrderResponse toResponse(Order order) {
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        Shipment shipment = shipmentRepository.findByOrderId(order.getId()).orElse(null);

        String addressSummary = null;
        if (order.getAddress() != null) {
            Address addr = order.getAddress();
            addressSummary = addr.getStreet() + ", " + addr.getNumber()
                    + " - " + addr.getNeighborhood() + ", " + addr.getCity() + " - " + addr.getState();
        }

        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> {
                    String productImage = null;
                    if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
                        productImage = item.getProduct().getImages().stream()
                                .filter(ProductImage::getPrimary)
                                .findFirst()
                                .map(ProductImage::getImageUrl)
                                .orElse(item.getProduct().getImages().iterator().next().getImageUrl());
                    }
                    return new OrderItemResponse(
                            item.getId(),
                            item.getProduct().getId(),
                            item.getProduct().getName(),
                            productImage,
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                    );
                })
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUser() != null ? order.getUser().getId() : null,
                null,
                order.getUser() != null ? order.getUser().getUsername() : null,
                order.getAddress() != null ? order.getAddress().getId() : null,
                addressSummary,
                order.getCoupon() != null ? order.getCoupon().getId() : null,
                order.getCoupon() != null ? order.getCoupon().getCode() : null,
                order.getStatus(),
                order.getSubtotal(),
                order.getDiscount(),
                order.getShippingCost(),
                order.getTotal(),
                order.getGuestName(),
                order.getGuestEmail(),
                order.getGuestPhone(),
                order.getGuestCpf(),
                items,
                payment != null ? payment.getId() : null,
                payment != null ? payment.getStatus() : null,
                payment != null ? payment.getPaymentMethod() : null,
                payment != null ? payment.getPaymentLink() : null,
                shipment != null ? shipment.getId() : null,
                shipment != null ? shipment.getStatus() : null,
                shipment != null ? shipment.getTrackingCode() : null,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
