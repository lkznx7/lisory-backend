package com.lisory.backend.pagamentos.asaas.provider;

import com.lisory.backend.config.properties.AsaasProperties;
import com.lisory.backend.exception.BusinessException;
import com.lisory.backend.pagamentos.asaas.client.AsaasClient;
import com.lisory.backend.pagamentos.asaas.dto.AsaasChargeRequest;
import com.lisory.backend.pagamentos.asaas.dto.AsaasChargeResponse;
import com.lisory.backend.pagamentos.asaas.dto.AsaasCustomerRequest;
import com.lisory.backend.pagamentos.asaas.dto.AsaasCustomerResponse;
import com.lisory.backend.pagamentos.services.GatewayResponse;
import com.lisory.backend.pagamentos.services.PaymentProvider;
import com.lisory.backend.pagamentos.services.PaymentRequest;
import com.lisory.backend.pedido.entity.Order;
import com.lisory.backend.pedido.repository.OrderRepository;
import com.lisory.backend.shared.log.StructuredLogger;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class AsaasPaymentProvider implements PaymentProvider {

    private static final StructuredLogger log = StructuredLogger.forClass(AsaasPaymentProvider.class);

    private final AsaasClient client;
    private final OrderRepository orderRepository;
    private final AsaasProperties properties;

    public AsaasPaymentProvider(AsaasClient client, OrderRepository orderRepository, AsaasProperties properties) {
        this.client = client;
        this.orderRepository = orderRepository;
        this.properties = properties;
    }

    @Override
    public GatewayResponse processPayment(PaymentRequest request) {
        log.info("asaas_process_payment", Map.of(
                "orderId", request.orderId().toString(),
                "amount", request.amount().toString(),
                "paymentMethod", request.paymentMethod()
        ));

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new BusinessException("Order not found: " + request.orderId()));

        String billingType = mapBillingType(request.paymentMethod());

        log.info("temporary_log_billing_type_gerado", Map.of(
                "billingType", billingType != null ? billingType : "null"
        ));

        AsaasCustomerResponse customer = findOrCreateCustomer(order);

        String dueDate = LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE);

        String redirectUrl = properties.redirectUrl() + "?orderId=" + order.getId();

        AsaasChargeRequest chargeRequest = new AsaasChargeRequest(
                customer.id(),
                billingType,
                request.amount(),
                dueDate,
                "Pedido " + order.getId(),
                order.getId().toString(),
                redirectUrl,
                null,
                null
        );

        log.info("temporary_log_payload_enviado_asaas", Map.of(
                "customerId", chargeRequest.customerId() != null ? chargeRequest.customerId() : "null",
                "billingType", chargeRequest.billingType() != null ? chargeRequest.billingType() : "null",
                "value", chargeRequest.value() != null ? chargeRequest.value().toString() : "null",
                "dueDate", chargeRequest.dueDate() != null ? chargeRequest.dueDate() : "null",
                "description", chargeRequest.description() != null ? chargeRequest.description() : "null",
                "externalReference", chargeRequest.externalReference() != null ? chargeRequest.externalReference() : "null"
        ));

        AsaasChargeResponse charge = client.createCharge(chargeRequest);
        if (charge == null || charge.id() == null || charge.id().isBlank()) {
            throw new BusinessException("Asaas returned a payment without id");
        }

        log.info("asaas_charge_created", Map.of(
                "paymentId", charge.id(),
                "status", charge.status(),
                "billingType", charge.billingType() != null ? charge.billingType() : "null"
        ));

        return new GatewayResponse(
                charge.id(),
                null,
                mapStatus(charge.status()),
                charge.invoiceUrl(),
                null,
                charge.qrCode(),
                charge.pixCopyAndPaste(),
                charge.transactionReceiptUrl()
        );
    }

    private String mapBillingType(String paymentMethod) {
        if (paymentMethod == null) return "PIX";
        return switch (paymentMethod.toUpperCase()) {
            case "PIX" -> "PIX";
            case "BOLETO", "BOL", "SLIP" -> "BOLETO";
            case "CREDIT_CARD", "CARTAO", "CARTAO_CREDITO", "CREDIT", "CARD" -> "CREDIT_CARD";
            default -> "PIX";
        };
    }

    private AsaasCustomerResponse findOrCreateCustomer(Order order) {
        String name = order.getGuestName();
        String email = order.getGuestEmail();
        String cpf = order.getGuestCpf();
        String phone = order.getGuestPhone();

        if (name == null || email == null) {
            throw new BusinessException("Customer name and email are required");
        }

        return client.findCustomerByCpfCnpj(cpf).orElseGet(() -> client.createCustomer(new AsaasCustomerRequest(
                name, email, cpf, phone
        )));
    }

    private String mapStatus(String asaasStatus) {
        if (asaasStatus == null) return "PENDING";
        return switch (asaasStatus.toUpperCase()) {
            case "PENDING" -> "PENDING";
            case "RECEIVED", "CONFIRMED" -> "APPROVED";
            case "OVERDUE" -> "EXPIRED";
            case "REFUNDED" -> "REFUNDED";
            case "CANCELLED", "DELETED" -> "CANCELLED";
            case "FAILED" -> "DECLINED";
            default -> "PENDING";
        };
    }
}
