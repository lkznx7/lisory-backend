package com.lisory.backend.pagamentos.asaas.client;

import com.lisory.backend.config.properties.AsaasProperties;
import com.lisory.backend.pagamentos.asaas.dto.AsaasChargeRequest;
import com.lisory.backend.pagamentos.asaas.dto.AsaasChargeResponse;
import com.lisory.backend.pagamentos.asaas.dto.AsaasCustomerRequest;
import com.lisory.backend.pagamentos.asaas.dto.AsaasCustomerResponse;
import com.lisory.backend.shared.log.StructuredLogger;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class AsaasClient {

    private static final StructuredLogger log = StructuredLogger.forClass(AsaasClient.class);

    private final RestClient restClient;
    private final AsaasProperties properties;

    public AsaasClient(RestClient.Builder restClientBuilder, AsaasProperties properties) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(properties.apiUrl())
                .defaultHeader("access_token", properties.apiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public AsaasCustomerResponse createCustomer(AsaasCustomerRequest request) {
        log.info("asaas_create_customer", Map.of("email", request.email()));

        return restClient.post()
                .uri("/customers")
                .body(request)
                .retrieve()
                .body(AsaasCustomerResponse.class);
    }

    public AsaasChargeResponse createCharge(AsaasChargeRequest request) {
        log.info("asaas_create_charge", Map.of(
                "externalReference", request.externalReference(),
                "value", request.value().toString(),
                "billingType", request.billingType()
        ));

        return restClient.post()
                .uri("/payments")
                .body(request)
                .retrieve()
                .body(AsaasChargeResponse.class);
    }

    public AsaasChargeResponse getCharge(String paymentId) {
        return restClient.get()
                .uri("/payments/{id}", paymentId)
                .retrieve()
                .body(AsaasChargeResponse.class);
    }
}
