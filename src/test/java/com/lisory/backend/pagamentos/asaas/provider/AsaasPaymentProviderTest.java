package com.lisory.backend.pagamentos.asaas.provider;

import com.lisory.backend.config.properties.AsaasProperties;
import com.lisory.backend.pagamentos.asaas.client.AsaasClient;
import com.lisory.backend.pagamentos.asaas.dto.AsaasChargeRequest;
import com.lisory.backend.pagamentos.asaas.dto.AsaasChargeResponse;
import com.lisory.backend.pagamentos.asaas.dto.AsaasCustomerResponse;
import com.lisory.backend.pagamentos.services.GatewayResponse;
import com.lisory.backend.pagamentos.services.PaymentRequest;
import com.lisory.backend.pedido.entity.Order;
import com.lisory.backend.pedido.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AsaasPaymentProvider Tests")
class AsaasPaymentProviderTest {

    @Mock
    private AsaasClient client;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AsaasProperties properties;

    @InjectMocks
    private AsaasPaymentProvider paymentProvider;

    private UUID orderId;
    private Order order;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        order = new Order();
        order.setId(orderId);
        order.setGuestName("John Doe");
        order.setGuestEmail("john@example.com");
        order.setGuestCpf("12345678909");
        order.setGuestPhone("61988888888");
        order.setTotal(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("should map 'card' to CREDIT_CARD billingType in Asaas")
    void shouldMapCardToCreditCard() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(properties.redirectUrl()).thenReturn("https://lisory.com.br/payment-return");

        AsaasCustomerResponse customerResponse = new AsaasCustomerResponse("cus-123", "John Doe", "john@example.com", "12345678909");
        when(client.findCustomerByCpfCnpj("12345678909")).thenReturn(Optional.of(customerResponse));

        AsaasChargeResponse chargeResponse = new AsaasChargeResponse(
                "pay-123", "PENDING", "https://invoice.url", "inv-123",
                new BigDecimal("100.00"), new BigDecimal("95.00"), "CREDIT_CARD",
                orderId.toString(), null, null, null, null, null, null
        );
        ArgumentCaptor<AsaasChargeRequest> captor = ArgumentCaptor.forClass(AsaasChargeRequest.class);
        when(client.createCharge(captor.capture())).thenReturn(chargeResponse);

        PaymentRequest paymentRequest = new PaymentRequest(orderId, new BigDecimal("100.00"), "card");
        GatewayResponse response = paymentProvider.processPayment(paymentRequest);

        assertNotNull(response);
        assertEquals("pay-123", response.gatewayId());
        assertEquals("PENDING", response.status());
        assertEquals("https://invoice.url", response.paymentUrl());

        AsaasChargeRequest capturedRequest = captor.getValue();
        assertEquals("CREDIT_CARD", capturedRequest.billingType());
    }

    @Test
    @DisplayName("should map 'pix' to PIX billingType in Asaas")
    void shouldMapPixToPix() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(properties.redirectUrl()).thenReturn("https://lisory.com.br/payment-return");

        AsaasCustomerResponse customerResponse = new AsaasCustomerResponse("cus-123", "John Doe", "john@example.com", "12345678909");
        when(client.findCustomerByCpfCnpj("12345678909")).thenReturn(Optional.of(customerResponse));

        AsaasChargeResponse chargeResponse = new AsaasChargeResponse(
                "pay-123", "PENDING", "https://invoice.url", "inv-123",
                new BigDecimal("100.00"), new BigDecimal("99.00"), "PIX",
                orderId.toString(), null, "qr-code-content", "pix-copy-paste-content", null, null, null
        );
        ArgumentCaptor<AsaasChargeRequest> captor = ArgumentCaptor.forClass(AsaasChargeRequest.class);
        when(client.createCharge(captor.capture())).thenReturn(chargeResponse);

        PaymentRequest paymentRequest = new PaymentRequest(orderId, new BigDecimal("100.00"), "pix");
        GatewayResponse response = paymentProvider.processPayment(paymentRequest);

        assertNotNull(response);
        assertEquals("pay-123", response.gatewayId());
        assertEquals("PENDING", response.status());
        assertEquals("qr-code-content", response.qrCode());
        assertEquals("pix-copy-paste-content", response.pixCopyAndPaste());

        AsaasChargeRequest capturedRequest = captor.getValue();
        assertEquals("PIX", capturedRequest.billingType());
    }
}
