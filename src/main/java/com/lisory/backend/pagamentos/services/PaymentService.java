package com.lisory.backend.pagamentos.services;

import com.lisory.backend.exception.ResourceNotFoundException;
import com.lisory.backend.pagamentos.dto.PaymentResponse;
import com.lisory.backend.pagamentos.entity.Payment;
import com.lisory.backend.pagamentos.repository.PaymentRepository;
import com.lisory.backend.pedido.entity.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    @PersistenceContext
    private EntityManager entityManager;

    public PaymentService(PaymentRepository paymentRepository, PaymentProvider paymentProvider) {
        this.paymentRepository = paymentRepository;
        this.paymentProvider = paymentProvider;
    }

    @Transactional
    public PaymentResponse initiatePayment(UUID orderId, String paymentMethod, BigDecimal amount) {
        log.info("Initiating payment for order {} with method {} and amount {}", orderId, paymentMethod, amount);

        Payment payment = new Payment();
        payment.setOrder(entityManager.getReference(Order.class, orderId));
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(amount);

        if ("PAGAR_NA_RETIRADA".equalsIgnoreCase(paymentMethod)) {
            payment.setStatus("PAGAMENTO_NA_RETIRADA");
            Payment saved = paymentRepository.save(payment);
            log.info("Payment initiated for pickup order {}: status=PAGAMENTO_NA_RETIRADA", orderId);
            return toDto(saved);
        }

        payment.setStatus("PROCESSING");
        Payment saved = paymentRepository.save(payment);

        PaymentRequest providerRequest = new PaymentRequest(orderId, amount, paymentMethod);
        GatewayResponse gatewayResponse = paymentProvider.processPayment(providerRequest);

        saved.setGatewayId(gatewayResponse.gatewayId());
        saved.setTransactionId(gatewayResponse.transactionId());
        saved.setGatewayPaymentId(gatewayResponse.gatewayId());
        saved.setStatus(gatewayResponse.status());
        if (gatewayResponse.paymentUrl() != null) {
            saved.setPaymentLink(gatewayResponse.paymentUrl());
        }
        if (gatewayResponse.qrCode() != null) {
            saved.setQrCode(gatewayResponse.qrCode());
        }
        if (gatewayResponse.pixCopyAndPaste() != null) {
            saved.setPixCopyAndPaste(gatewayResponse.pixCopyAndPaste());
        }
        if (gatewayResponse.transactionReceiptUrl() != null) {
            saved.setTransactionReceiptUrl(gatewayResponse.transactionReceiptUrl());
        }
        saved = paymentRepository.save(saved);

        log.info("Payment initiated for order {}: status={}, hasPaymentLink={}",
                orderId, saved.getStatus(), saved.getPaymentLink() != null);

        return toDto(saved);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public GatewayResponse processOrderPayment(UUID orderId, String paymentMethod, BigDecimal amount) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);

        if (payment == null) {
            payment = new Payment();
        }
        payment.setOrder(entityManager.getReference(Order.class, orderId));
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(amount);

        if ("PAGAR_NA_RETIRADA".equalsIgnoreCase(paymentMethod)) {
            payment.setStatus("PAGAMENTO_NA_RETIRADA");
            payment = paymentRepository.save(payment);
            log.info("Payment processed for pickup order {}: status=PAGAMENTO_NA_RETIRADA", orderId);
            return new GatewayResponse("PICKUP", "PICKUP", "PAGAMENTO_NA_RETIRADA", null, null, null, null, null);
        }

        payment.setStatus("PROCESSING");
        payment = paymentRepository.save(payment);

        PaymentRequest providerRequest = new PaymentRequest(orderId, amount, paymentMethod);
        GatewayResponse gatewayResponse = paymentProvider.processPayment(providerRequest);

        payment.setGatewayId(gatewayResponse.gatewayId());
        payment.setTransactionId(gatewayResponse.transactionId());
        payment.setGatewayPaymentId(gatewayResponse.gatewayId());
        payment.setStatus(gatewayResponse.status());
        if (gatewayResponse.paymentUrl() != null) {
            payment.setPaymentLink(gatewayResponse.paymentUrl());
        }
        if (gatewayResponse.slug() != null) {
            payment.setOrderNSU(gatewayResponse.slug());
        }
        if (gatewayResponse.qrCode() != null) {
            payment.setQrCode(gatewayResponse.qrCode());
        }
        if (gatewayResponse.pixCopyAndPaste() != null) {
            payment.setPixCopyAndPaste(gatewayResponse.pixCopyAndPaste());
        }
        if (gatewayResponse.transactionReceiptUrl() != null) {
            payment.setTransactionReceiptUrl(gatewayResponse.transactionReceiptUrl());
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
                payment.getQrCode(),
                payment.getPixCopyAndPaste(),
                payment.getTransactionReceiptUrl(),
                payment.getExpirationDate(),
                payment.getAuthorizationCode(),
                payment.getInstallments(),
                payment.getPaidAt(),
                payment.getCreatedAt()
        );
    }
}
