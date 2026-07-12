package com.lisory.backend.pagamentos.services;

import com.lisory.backend.exception.ResourceNotFoundException;
import com.lisory.backend.pagamentos.dto.PaymentResponse;
import com.lisory.backend.pagamentos.entity.Payment;
import com.lisory.backend.pagamentos.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProvider paymentProvider;

    @InjectMocks
    private PaymentService paymentService;

    private UUID orderId;
    private UUID paymentId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        paymentId = UUID.randomUUID();
    }

    @Test
    @DisplayName("should initiate payment and create payment link")
    void shouldInitiatePayment() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(paymentId);
            return payment;
        });

        GatewayResponse gatewayResponse = new GatewayResponse(
                orderId.toString(), orderId.toString(), "PENDING",
                "https://pay.asaas.com/test?invoice=abc123");
        when(paymentProvider.processPayment(any())).thenReturn(gatewayResponse);

        PaymentResponse response = paymentService.initiatePayment(orderId, "PIX", new BigDecimal("149.00"));

        assertNotNull(response);
        assertEquals("PENDING", response.status());
        assertEquals("PIX", response.paymentMethod());
        assertEquals(new BigDecimal("149.00"), response.amount());
        assertNotNull(response.paymentLink());
        assertTrue(response.paymentLink().contains("asaas"));
        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(paymentProvider, times(1)).processPayment(any());
    }

    @Test
    @DisplayName("should process payment successfully")
    void shouldProcessPayment() {
        Payment existingPayment = new Payment();
        existingPayment.setId(paymentId);
        com.lisory.backend.pedido.entity.Order order = new com.lisory.backend.pedido.entity.Order();
        order.setId(orderId);
        existingPayment.setOrder(order);
        existingPayment.setPaymentMethod("PIX");
        existingPayment.setAmount(new BigDecimal("149.00"));
        existingPayment.setStatus("PROCESSING");

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        GatewayResponse gatewayResponse = new GatewayResponse(
                "gw-123", "tx-456", "APPROVED", null);
        when(paymentProvider.processPayment(any())).thenReturn(gatewayResponse);

        GatewayResponse result = paymentService.processOrderPayment(orderId, "PIX", new BigDecimal("149.00"));

        assertNotNull(result);
        assertEquals("APPROVED", result.status());
        assertEquals("gw-123", result.gatewayId());
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when payment not found")
    void shouldThrowWhenPaymentNotFound() {
        UUID nonExistentOrderId = UUID.randomUUID();
        when(paymentRepository.findByOrderId(nonExistentOrderId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.getPaymentByOrderId(nonExistentOrderId));
    }

    @Test
    @DisplayName("should return payment response with payment link")
    void shouldReturnPaymentResponse() {
        Payment payment = new Payment();
        payment.setId(paymentId);
        com.lisory.backend.pedido.entity.Order order = new com.lisory.backend.pedido.entity.Order();
        order.setId(orderId);
        payment.setOrder(order);
        payment.setPaymentMethod("CARTAO");
        payment.setAmount(new BigDecimal("259.00"));
        payment.setStatus("APPROVED");
        payment.setGatewayId("gw-789");
        payment.setTransactionId("tx-012");
        payment.setPaidAt(LocalDateTime.now());
        payment.setInstallments(3);
        payment.setPaymentLink("https://pay.asaas.com/test?invoice=xyz");

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);

        assertEquals(paymentId, response.id());
        assertEquals("CARTAO", response.paymentMethod());
        assertEquals(new BigDecimal("259.00"), response.amount());
        assertEquals("APPROVED", response.status());
        assertEquals(3, response.installments());
        assertNotNull(response.paymentLink());
    }
}
