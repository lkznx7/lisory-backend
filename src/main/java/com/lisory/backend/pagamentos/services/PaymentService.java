package com.lisory.backend.pagamentos.services;

import com.lisory.backend.exception.ResourceNotFoundException;
import com.lisory.backend.pagamentos.dto.PaymentResponse;
import com.lisory.backend.pagamentos.entity.Payment;
import com.lisory.backend.pagamentos.repository.PaymentRepository;
import com.lisory.backend.pedido.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentProvider paymentProvider;

    public PaymentService(PaymentRepository paymentRepository, PaymentProvider paymentProvider) {
        this.paymentRepository = paymentRepository;
        this.paymentProvider = paymentProvider;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentResponse initiatePayment(UUID orderId, String paymentMethod, BigDecimal amount) {
        log.info("Initiating payment for order {} with method {} and amount {}", orderId, paymentMethod, amount);

        Payment payment = new Payment();
        Order orderStub = new Order();
        orderStub.setId(orderId);
        payment.setOrder(orderStub);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(amount);
        payment.setStatus("PROCESSING");
        Payment saved = paymentRepository.save(payment);

        PaymentRequest providerRequest = new PaymentRequest(orderId, amount, paymentMethod);
        GatewayResponse gatewayResponse = paymentProvider.processPayment(providerRequest);

        saved.setGatewayId(gatewayResponse.gatewayId());
        saved.setTransactionId(gatewayResponse.transactionId());
        saved.setStatus(gatewayResponse.status());
        if (gatewayResponse.paymentUrl() != null) {
            saved.setPaymentLink(gatewayResponse.paymentUrl());
        }
        if (gatewayResponse.slug() != null) {
            saved.setOrderNSU(gatewayResponse.slug());
        }
        saved = paymentRepository.save(saved);

        log.info("Payment initiated for order {}: status={}, hasPaymentLink={}",
                orderId, saved.getStatus(), saved.getPaymentLink() != null);

        return toDto(saved);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public GatewayResponse processOrderPayment(UUID orderId, String paymentMethod, BigDecimal amount) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);

        if (payment != null) {
            payment.setPaymentMethod(paymentMethod);
            payment.setAmount(amount);
            payment.setStatus("PROCESSING");
        } else {
            payment = new Payment();
            Order orderStub = new Order();
            orderStub.setId(orderId);
            payment.setOrder(orderStub);
            payment.setPaymentMethod(paymentMethod);
            payment.setAmount(amount);
            payment.setStatus("PROCESSING");
        }
        payment = paymentRepository.save(payment);

        PaymentRequest providerRequest = new PaymentRequest(orderId, amount, paymentMethod);
        GatewayResponse gatewayResponse = paymentProvider.processPayment(providerRequest);

        payment.setGatewayId(gatewayResponse.gatewayId());
        payment.setTransactionId(gatewayResponse.transactionId());
        payment.setStatus(gatewayResponse.status());
        if (gatewayResponse.paymentUrl() != null) {
            payment.setPaymentLink(gatewayResponse.paymentUrl());
        }
        if (gatewayResponse.slug() != null) {
            payment.setOrderNSU(gatewayResponse.slug());
        }
        if ("APPROVED".equals(gatewayResponse.status()) || "PAID".equals(gatewayResponse.status())) {
            payment.setPaidAt(LocalDateTime.now());
        }
        payment = paymentRepository.save(payment);

        return gatewayResponse;
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return toDto(payment);
    }

    private PaymentResponse toDto(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder() != null ? payment.getOrder().getId() : null,
                payment.getPaymentMethod(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getGatewayId(),
                payment.getTransactionId(),
                payment.getGatewayPaymentId(),
                payment.getGatewayOrderId(),
                payment.getOrderNSU(),
                payment.getTransactionNSU(),
                payment.getPaymentLink(),
                payment.getExpirationDate(),
                payment.getAuthorizationCode(),
                payment.getInstallments(),
                payment.getPaidAt(),
                payment.getCreatedAt()
        );
    }
}
