package com.lisory.backend.pagamentos.asaas.client;

import com.lisory.backend.config.properties.AsaasProperties;
import com.lisory.backend.pagamentos.asaas.dto.AsaasChargeRequest;
import com.lisory.backend.pagamentos.asaas.dto.AsaasChargeResponse;
import com.lisory.backend.pagamentos.asaas.dto.AsaasCustomerListResponse;
import com.lisory.backend.pagamentos.asaas.dto.AsaasCustomerRequest;
import com.lisory.backend.pagamentos.asaas.dto.AsaasCustomerResponse;
import com.lisory.backend.pagamentos.asaas.exception.AsaasException;
import com.lisory.backend.shared.log.StructuredLogger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AsaasClient {

    private static final StructuredLogger log = StructuredLogger.forClass(AsaasClient.class);

    private final RestClient restClient;
    private final AsaasProperties properties;

    public AsaasClient(RestClient.Builder restClientBuilder, AsaasProperties properties) {
        this.properties = properties;
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new IllegalStateException("ASAAS_API_KEY must be configured");
        }
        this.restClient = restClientBuilder
                .baseUrl(properties.apiUrl())
                .defaultHeader("access_token", properties.apiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "Lisory/1.0")
                .build();

    }

    public AsaasCustomerResponse createCustomer(AsaasCustomerRequest request) {
        log.info("asaas_create_customer", Map.of("email", request.email()));

        return restClient.post()
                .uri("/customers")
                .body(request)
                .retrieve()
                .onStatus(status -> status.isError(), (request2, response) -> {
                    String body = readBody(response);
                    log.error("asaas_create_customer_error", Map.of(
                            "status", String.valueOf(response.getStatusCode().value()),
                            "body", body
                    ));
                    throw asaasError("creating customer", response.getStatusCode().value(), body);
                })
                .body(AsaasCustomerResponse.class);
    }

    public Optional<AsaasCustomerResponse> findCustomerByCpfCnpj(String cpfCnpj) {
        if (cpfCnpj == null || cpfCnpj.isBlank()) {
            return Optional.empty();
        }

        AsaasCustomerListResponse result = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/customers").queryParam("cpfCnpj", cpfCnpj).build())
                .retrieve()
                .onStatus(status -> status.isError(), (request, response) -> {
                    String body = readBody(response);
                    log.error("asaas_find_customer_error", Map.of("status", String.valueOf(response.getStatusCode().value()), "body", body));
                    throw asaasError("looking up customer", response.getStatusCode().value(), body);
                })
                .body(AsaasCustomerListResponse.class);
        return result == null || result.data() == null ? Optional.empty() : result.data().stream().findFirst();
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
                .onStatus(status -> status.isError(), (request2, response) -> {
                    String body = readBody(response);
                    log.error("asaas_create_charge_error", Map.of(
                            "status", String.valueOf(response.getStatusCode().value()),
                            "body", body
                    ));
                    throw asaasError("creating payment", response.getStatusCode().value(), body);
                })
                .body(AsaasChargeResponse.class);
    }

    public AsaasChargeResponse getCharge(String paymentId) {
        return restClient.get()
                .uri("/payments/{id}", paymentId)
                .retrieve()
                .onStatus(status -> status.isError(), (request2, response) -> {
                    String body = readBody(response);
                    log.error("asaas_get_charge_error", Map.of(
                            "paymentId", paymentId,
                            "status", String.valueOf(response.getStatusCode().value()),
                            "body", body
                    ));
                    throw asaasError("fetching payment", response.getStatusCode().value(), body);
                })
                .body(AsaasChargeResponse.class);
    }

    private String readBody(ClientHttpResponse response) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "(could not read body)";
        }
    }

    private AsaasException asaasError(String operation, int status, String body) {
        return new AsaasException("Asaas could not complete " + operation + " (HTTP " + status + ")");
    }

}
