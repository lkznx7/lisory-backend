package com.lisory.backend.pagamentos.asaas.webhook;

import com.lisory.backend.pagamentos.asaas.dto.AsaasWebhookEvent;
import com.lisory.backend.pagamentos.entity.Payment;
import com.lisory.backend.pagamentos.repository.PaymentRepository;
import com.lisory.backend.pedido.entity.Order;
import com.lisory.backend.pedido.entity.OrderStatus;
import com.lisory.backend.pedido.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AsaasWebhookService Tests")
class AsaasWebhookServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private AsaasWebhookService webhookService;

    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("should process PAYMENT_RECEIVED webhook and approve payment")
    void shouldProcessPaymentReceived() {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        Order order = new Order();
        order.setId(orderId);
        payment.setOrder(order);
        payment.setStatus("PENDING");

        Order orderEntity = new Order();
        orderEntity.setId(orderId);
        orderEntity.setStatus(OrderStatus.AGUARDANDO_PAGAMENTO.name());

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        AsaasWebhookEvent event = new AsaasWebhookEvent(
                "PAYMENT_RECEIVED",
                new AsaasWebhookEvent.PaymentData(
                        "pay_abc123", "RECEIVED", orderId.toString(),
                        150.0, 142.5, "PIX",
                        "https://pay.asaas.com/invoice/abc", "12345"
                )
        );

        webhookService.processEvent(event);

        verify(paymentRepository, times(1)).save(any(Payment.class));
        assertEquals("APPROVED", payment.getStatus());
        assertNotNull(payment.getPaidAt());
        assertEquals("pay_abc123", payment.getGatewayPaymentId());
        assertEquals(OrderStatus.PAGO.name(), orderEntity.getStatus());
    }

    @Test
    @DisplayName("should skip idempotent events")
    void shouldSkipIdempotentEvents() {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        Order order = new Order();
        order.setId(orderId);
        payment.setOrder(order);
        payment.setStatus("APPROVED");

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        AsaasWebhookEvent event = new AsaasWebhookEvent(
                "PAYMENT_RECEIVED",
                new AsaasWebhookEvent.PaymentData(
                        "pay_abc123", "RECEIVED", orderId.toString(),
                        150.0, 142.5, "PIX", null, null
                )
        );

        webhookService.processEvent(event);

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("should handle missing external reference")
    void shouldHandleMissingExternalReference() {
        AsaasWebhookEvent event = new AsaasWebhookEvent(
                "PAYMENT_RECEIVED",
                new AsaasWebhookEvent.PaymentData(
                        "pay_abc123", "RECEIVED", null,
                        150.0, 142.5, "PIX", null, null
                )
        );

        webhookService.processEvent(event);

        verify(paymentRepository, never()).findByOrderId(any());
    }

    @Test
    @DisplayName("should handle payment not found")
    void shouldHandlePaymentNotFound() {
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        AsaasWebhookEvent event = new AsaasWebhookEvent(
                "PAYMENT_RECEIVED",
                new AsaasWebhookEvent.PaymentData(
                        "pay_abc123", "RECEIVED", orderId.toString(),
                        150.0, 142.5, "PIX", null, null
                )
        );

        webhookService.processEvent(event);

        verify(paymentRepository, never()).save(any());
    }
}
