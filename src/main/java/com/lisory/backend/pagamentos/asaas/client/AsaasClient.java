package com.lisory.backend.pagamentos.asaas.client;

import com.lisory.backend.config.properties.AsaasProperties;
import com.lisory.backend.pagamentos.asaas.dto.AsaasChargeRequest;
import com.lisory.backend.pagamentos.asaas.dto.AsaasChargeResponse;
import com.lisory.backend.pagamentos.asaas.dto.AsaasCustomerListResponse;
import com.lisory.backend.pagamentos.asaas.dto.AsaasCustomerRequest;
import com.lisory.backend.pagamentos.asaas.dto.AsaasCustomerResponse;
import com.lisory.backend.pagamentos.asaas.exception.AsaasException;
import com.lisory.backend.shared.log.StructuredLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AsaasClient {

    private static final Logger log = LoggerFactory.getLogger(AsaasClient.class);

    private final RestClient restClient;
    private final AsaasProperties properties;

    public AsaasClient(RestClient.Builder restClientBuilder, AsaasProperties properties) {
        this.properties = properties;

        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new IllegalStateException("ASAAS_API_KEY must be configured");
        }

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(30));

        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                .baseUrl(properties.apiUrl())
                .defaultHeader("access_token", properties.apiKey())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        String maskedKey = maskKey(properties.apiKey());
        log.info("[ASAAS] Client initialized | baseUrl={} | access_token={}", properties.apiUrl(), maskedKey);
    }

    public AsaasCustomerResponse createCustomer(AsaasCustomerRequest request) {
        log.info("[ASAAS] POST {}/customers | body={}", properties.apiUrl(), request);

        AsaasCustomerResponse response = restClient.post()
                .uri("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(status -> status.isError(), (req, httpResponse) -> {
                    String body = readBody(httpResponse);
                    log.error("[ASAAS] POST {}/customers FAILED | status={} | body={}",
                            properties.apiUrl(), httpResponse.getStatusCode().value(), body);
                    throw asaasError("creating customer", httpResponse.getStatusCode().value(), body);
                })
                .body(AsaasCustomerResponse.class);

        log.info("[ASAAS] POST {}/customers OK | customerId={}", properties.apiUrl(), response != null ? response.id() : "null");
        return response;
    }

    public Optional<AsaasCustomerResponse> findCustomerByCpfCnpj(String cpfCnpj) {
        if (cpfCnpj == null || cpfCnpj.isBlank()) {
            return Optional.empty();
        }

        log.info("[ASAAS] GET {}/customers?cpfCnpj={}", properties.apiUrl(), cpfCnpj);

        AsaasCustomerListResponse result = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/customers").queryParam("cpfCnpj", cpfCnpj).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status.isError(), (req, response) -> {
                    String body = readBody(response);
                    log.error("[ASAAS] GET {}/customers FAILED | status={} | body={}",
                            properties.apiUrl(), response.getStatusCode().value(), body);
                    throw asaasError("looking up customer", response.getStatusCode().value(), body);
                })
                .body(AsaasCustomerListResponse.class);

        Optional<AsaasCustomerResponse> found = result == null || result.data() == null
                ? Optional.empty()
                : result.data().stream().findFirst();

        log.info("[ASAAS] GET {}/customers OK | found={}", properties.apiUrl(), found.isPresent());
        return found;
    }

    public AsaasChargeResponse createCharge(AsaasChargeRequest request) {
        log.info("[ASAAS] POST {}/payments | body={}", properties.apiUrl(), request);

        AsaasChargeResponse response = restClient.post()
                .uri("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(status -> status.isError(), (req, resp) -> {
                    String body = readBody(resp);
                    log.error("[ASAAS] POST {}/payments FAILED | status={} | body={}",
                            properties.apiUrl(), resp.getStatusCode().value(), body);
                    throw asaasError("creating payment", resp.getStatusCode().value(), body);
                })
                .body(AsaasChargeResponse.class);

        log.info("[ASAAS] POST {}/payments OK | paymentId={} | status={} | invoiceUrl={}",
                properties.apiUrl(),
                response != null ? response.id() : "null",
                response != null ? response.status() : "null",
                response != null ? response.invoiceUrl() : "null");
        return response;
    }

    public AsaasChargeResponse getCharge(String paymentId) {
        log.info("[ASAAS] GET {}/payments/{}", properties.apiUrl(), paymentId);

        AsaasChargeResponse response = restClient.get()
                .uri("/payments/{id}", paymentId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status.isError(), (req, httpResponse) -> {
                    String body = readBody(httpResponse);
                    log.error("[ASAAS] GET {}/payments/{} FAILED | status={} | body={}",
                            properties.apiUrl(), paymentId, httpResponse.getStatusCode().value(), body);
                    throw asaasError("fetching payment", httpResponse.getStatusCode().value(), body);
                })
                .body(AsaasChargeResponse.class);

        log.info("[ASAAS] GET {}/payments/{} OK | status={}", properties.apiUrl(), paymentId,
                response != null ? response.status() : "null");
        return response;
    }

    public void cancelCharge(String paymentId) {
        log.info("[ASAAS] POST {}/payments/{}/cancel", properties.apiUrl(), paymentId);

        restClient.post()
                .uri("/payments/{id}/cancel", paymentId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status.isError(), (req, response) -> {
                    String body = readBody(response);
                    log.error("[ASAAS] POST {}/payments/{}/cancel FAILED | status={} | body={}",
                            properties.apiUrl(), paymentId, response.getStatusCode().value(), body);
                    throw asaasError("cancelling payment", response.getStatusCode().value(), body);
                })
                .body(Void.class);

        log.info("[ASAAS] POST {}/payments/{}/cancel OK", properties.apiUrl(), paymentId);
    }

    private String readBody(org.springframework.http.client.ClientHttpResponse response) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "(could not read body)";
        }
    }

    private AsaasException asaasError(String operation, int status, String body) {
        return new AsaasException(operation, status, body);
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 20) return "****";
        return key.substring(0, 10) + "..." + key.substring(key.length() - 10);
    }

}
