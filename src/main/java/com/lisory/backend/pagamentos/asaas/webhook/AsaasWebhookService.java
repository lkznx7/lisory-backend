package com.lisory.backend.pagamentos.asaas.webhook;

import com.lisory.backend.pagamentos.asaas.dto.AsaasWebhookEvent;
import com.lisory.backend.pagamentos.entity.Payment;
import com.lisory.backend.pagamentos.repository.PaymentRepository;
import com.lisory.backend.pedido.entity.Order;
import com.lisory.backend.pedido.entity.OrderStatus;
import com.lisory.backend.pedido.repository.OrderRepository;
import com.lisory.backend.shared.log.StructuredLogger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AsaasWebhookService {

    private static final StructuredLogger log = StructuredLogger.forClass(AsaasWebhookService.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public AsaasWebhookService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void processEvent(AsaasWebhookEvent event) {
        log.info("asaas_webhook_received", Map.of(
                "event", event.event(),
                "paymentId", event.payment() != null ? event.payment().id() : "null"
        ));

        if (event.payment() == null) return;

        String externalReference = event.payment().externalReference();
        if (externalReference == null || externalReference.isBlank()) return;

        UUID orderId;
        try {
            orderId = UUID.fromString(externalReference);
        } catch (IllegalArgumentException e) {
            log.warn("asaas_webhook_invalid_external_reference", Map.of("externalReference", externalReference));
            return;
        }

        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (payment == null) {
            log.warn("asaas_webhook_payment_not_found", Map.of("orderId", orderId.toString()));
            return;
        }

        String newStatus = mapWebhookEventToStatus(event.event());
        if (newStatus == null) {
            log.info("asaas_webhook_event_ignored", Map.of("event", event.event()));
            return;
        }

        if ("APPROVED".equals(payment.getStatus())) {
            log.info("asaas_webhook_already_approved", Map.of("orderId", orderId.toString()));
            return;
        }

        payment.setStatus(newStatus);
        payment.setGatewayPaymentId(event.payment().id());

        if (event.payment().invoiceUrl() != null) {
            payment.setPaymentLink(event.payment().invoiceUrl());
        }
        if (event.payment().qrCode() != null) {
            payment.setQrCode(event.payment().qrCode());
        }
        if (event.payment().pixCopyAndPaste() != null) {
            payment.setPixCopyAndPaste(event.payment().pixCopyAndPaste());
        }
        if (event.payment().transactionReceiptUrl() != null) {
            payment.setTransactionReceiptUrl(event.payment().transactionReceiptUrl());
        }

        if ("APPROVED".equals(newStatus)) {
            payment.setPaidAt(LocalDateTime.now());
        }
        paymentRepository.save(payment);

        if ("APPROVED".equals(newStatus)) {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null && OrderStatus.PENDING_PAYMENT.name().equals(order.getStatus())) {
                order.setStatus(OrderStatus.PAGO.name());
                orderRepository.save(order);
                log.info("asaas_webhook_order_updated", Map.of(
                        "orderId", orderId.toString(),
                        "newStatus", OrderStatus.PAGO.name()
                ));
            }
        }

        if ("CANCELLED".equals(newStatus)) {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                OrderStatus currentOrderStatus = OrderStatus.valueOf(order.getStatus());
                if (currentOrderStatus == OrderStatus.PENDING_PAYMENT) {
                    order.setStatus(OrderStatus.CANCELADO.name());
                    orderRepository.save(order);
                    log.info("asaas_webhook_order_cancelled", Map.of(
                            "orderId", orderId.toString()
                    ));
                }
            }
        }

        log.info("asaas_webhook_processed", Map.of(
                "orderId", orderId.toString(),
                "paymentStatus", newStatus
        ));
    }

    private String mapWebhookEventToStatus(String event) {
        if (event == null) return null;
        return switch (event.toUpperCase()) {
            case "PAYMENT_RECEIVED", "PAYMENT_CONFIRMED" -> "APPROVED";
            case "PAYMENT_CREATED" -> "PENDING";
            case "PAYMENT_OVERDUE" -> "EXPIRED";
            case "PAYMENT_REFUNDED" -> "REFUNDED";
            case "PAYMENT_DELETED" -> "CANCELLED";
            case "PAYMENT_FAILED" -> "DECLINED";
            default -> null;
        };
    }
}
