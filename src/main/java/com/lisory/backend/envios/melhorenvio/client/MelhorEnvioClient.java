package com.lisory.backend.envios.melhorenvio.client;

import com.lisory.backend.config.properties.MelhorEnvioProperties;
import com.lisory.backend.envios.melhorenvio.dto.*;
import com.lisory.backend.envios.melhorenvio.exception.MelhorEnvioException;
import com.lisory.backend.envios.melhorenvio.service.MelhorEnvioOAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

// TEMPORÁRIO - TESTES MELHOR ENVIO
// REMOVER ANTES DE PRODUÇÃO
// Todas as URLs, token e headers estão hardcoded para isolar o teste de integração.
@Component
public class MelhorEnvioClient {

    private static final Logger log = LoggerFactory.getLogger(MelhorEnvioClient.class);

    // TEMPORÁRIO - TESTES MELHOR ENVIO
    // REMOVER ANTES DE PRODUÇÃO
    private static final String BASE_URL = "https://melhorenvio.com.br";
    private static final String ACCESS_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiMTE2ZWNjNDc0YjI5MWY5MjhhMGQ0NTk3OTEwZjM3OTdiMDkyYTYzYTU4Y2Y3N2Y4MGNkMmU4MjgwZjFlMjZiMGFiYzNkMGU5NzcxNzA3MDQiLCJpYXQiOjE3ODQwMzE3MDQuMDM5NjQ0LCJuYmYiOjE3ODQwMzE3MDQuMDM5NjQ2LCJleHAiOjE4MTU1Njc3MDQuMDIzMzEsInN1YiI6ImJjYTgwODA3LTExZmItNDFjNC1iZTU5LThmNjVjMDVmODNhMSIsInNjb3BlcyI6WyJjYXJ0LXJlYWQiLCJjYXJ0LXdyaXRlIiwiY29tcGFuaWVzLXJlYWQiLCJjb21wYW5pZXMtd3JpdGUiLCJjb3Vwb25zLXJlYWQiLCJjb3Vwb25zLXdyaXRlIiwibm90aWZpY2F0aW9ucy1yZWFkIiwib3JkZXJzLXJlYWQiLCJwcm9kdWN0cy1yZWFkIiwicHJvZHVjdHMtZGVzdHJveSIsInByb2R1Y3RzLXdyaXRlIiwicHVyY2hhc2VzLXJlYWQiLCJzaGlwcGluZy1jYWxjdWxhdGUiLCJzaGlwcGluZy1jYW5jZWwiLCJzaGlwcGluZy1jaGVja291dCIsInNoaXBwaW5nLWNvbXBhbmllcyIsInNoaXBwaW5nLWdlbmVyYXRlIiwic2hpcHBpbmctcHJldmlldyIsInNoaXBwaW5nLXByaW50Iiwic2hpcHBpbmctc2hhcmUiLCJzaGlwcGluZy10cmFja2luZyIsImVjb21tZXJjZS1zaGlwcGluZyIsInRyYW5zYWN0aW9ucy1yZWFkIiwidXNlcnMtcmVhZCIsInVzZXJzLXdyaXRlIiwid2ViaG9va3MtcmVhZCIsIndlYmhvb2tzLXdyaXRlIiwid2ViaG9va3MtZGVsZXRlIiwidGRlYWxlci13ZWJob29rIl19.OTeWUQXnFsPby887Ugz8j0H9wMIiaGWu9itU1OJ8gxAqMs9up1UsOjisOqnNkrwQaUAtysQkAUZqVY_z_a-L2pd8XLgj8m7RyL-wvyOoK8uLJvsGnzxARy52Xq88WED2MRa8S_nRMDX_Eb7AreqjEk3jhD3z79KjuNRPkEpsqse9reMkYAAx4-lfzi8mSsPI4tYE09rDpCIozmqEgwkXm_1lTMHMBT7gWPBiRSqyew_cSGIcuNJOGWJTAKXSWR-jp-XerPGIoDyRyNRC_-2FgY3psr_dz8OtXul72FUhGv0RMr6zNPr_OD_eQkK80Qyi82ktakcYQrRArimBGXRBTssyJ7lrrY4EK3RLLy6GQms1cZdCbglh52XZBWIvdc2g7a6qXT89fX2kNGeiEHbLm0GfzVU0xT4SmFiW-OKOWxNaQcdDLc3HyRO42ZnbO5kefbdZ1jD_aUtMI1sqj8UZKvU0gCDNNEZC5MD7Xwq-aN4vhXgPYSDNDXMiKDfuaONaeSATeOMG0qNN9hfmbjdEq81h4ZwYg0w-lLkVppICXlnX4tBX1Oe506PvbAf4_lgy4OrAoBIpZLsBnMAslIcb-a-aoc0bDMuCC2q8u_emWvJzNywh5MBoDRU4mCXgjG-vmKpOpBcTCx6MVZA9qcEWQmJmeOq6ZUwTxZimkjk2XXw";
    private static final String URL_CALCULATE = BASE_URL + "/api/v2/me/shipment/calculate";
    private static final String URL_CART = BASE_URL + "/api/v2/me/cart";
    private static final String URL_CHECKOUT = BASE_URL + "/api/v2/me/shipment/checkout";
    private static final String URL_GENERATE = BASE_URL + "/api/v2/me/shipment/generate";
    private static final String URL_PRINT = BASE_URL + "/api/v2/me/shipment/print";
    private static final String URL_SHIPMENT_PREFIX = BASE_URL + "/api/v2/me/shipment/";
    private static final String USER_AGENT = "Lisory (contato@lisory.com.br)";
    // FIM TEMPORÁRIO

    private final RestTemplate restTemplate;

    // TEMPORÁRIO - TESTES MELHOR ENVIO
    // REMOVER ANTES DE PRODUÇÃO
    // Construtor mantido para compatibilidade com Spring, mas dependências não são utilizadas.
    @SuppressWarnings("unused")
    private final MelhorEnvioOAuthService oAuthService;
    @SuppressWarnings("unused")
    private final MelhorEnvioProperties properties;
    // FIM TEMPORÁRIO

    public MelhorEnvioClient(
            RestTemplate defaultRestTemplate,
            MelhorEnvioOAuthService oAuthService,
            MelhorEnvioProperties properties
    ) {
        this.restTemplate = defaultRestTemplate;
        this.oAuthService = oAuthService;
        this.properties = properties;
    }

    public List<MelhorEnvioCalculateResponse> calculateShipping(MelhorEnvioCalculateRequest request) {
        String url = URL_CALCULATE;
        log.info("Calculating shipping from {} to {} [URL: {}]", request.from().postalCode(), request.to().postalCode(), url);

        try {
            HttpEntity<MelhorEnvioCalculateRequest> entity = new HttpEntity<>(request, createHeaders());
            ResponseEntity<List<MelhorEnvioCalculateResponse>> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity,
                    new org.springframework.core.ParameterizedTypeReference<List<MelhorEnvioCalculateResponse>>() {}
            );

            log.info("Shipping calculation completed: HTTP {} bodySize={}", response.getStatusCode(),
                    response.getBody() != null ? response.getBody().size() : 0);
            if (response.getBody() != null) {
                for (MelhorEnvioCalculateResponse opt : response.getBody()) {
                    log.info("  option id={} name={} company={} price={} error={}",
                            opt.id(), opt.name(),
                            opt.company() != null ? opt.company().name() : "null",
                            opt.price(), opt.error());
                }
            }
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error calculating shipping: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new MelhorEnvioException(
                    "Failed to calculate shipping: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(),
                    "CALCULATE_SHIPPING_ERROR"
            );
        } catch (Exception e) {
            log.error("Error calculating shipping", e);
            throw new MelhorEnvioException("Error calculating shipping: " + e.getMessage(), e);
        }
    }

    public MelhorEnvioLabelResponse addToCart(MelhorEnvioCartRequest request) {
        // TEMPORÁRIO - TESTES MELHOR ENVIO - URL hardcoded
        String url = URL_CART;
        log.info("Adding shipment to cart for service {}", request.service());

        try {
            HttpEntity<MelhorEnvioCartRequest> entity = new HttpEntity<>(request, createHeaders());
            ResponseEntity<MelhorEnvioLabelResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, MelhorEnvioLabelResponse.class
            );

            log.info("Shipment added to cart successfully, protocol: {}", response.getBody() != null ? response.getBody().protocol() : "N/A");
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error adding to cart: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new MelhorEnvioException(
                    "Failed to add to cart: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(),
                    "ADD_TO_CART_ERROR"
            );
        } catch (Exception e) {
            log.error("Error adding to cart", e);
            throw new MelhorEnvioException("Error adding to cart: " + e.getMessage(), e);
        }
    }

    public List<MelhorEnvioLabelResponse> checkoutCart(MelhorEnvioCheckoutRequest request) {
        // TEMPORÁRIO - TESTES MELHOR ENVIO - URL hardcoded
        String url = URL_CHECKOUT;
        log.info("Checking out cart with service {}", request.service());

        try {
            HttpEntity<MelhorEnvioCheckoutRequest> entity = new HttpEntity<>(request, createHeaders());
            ResponseEntity<List<MelhorEnvioLabelResponse>> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity,
                    new org.springframework.core.ParameterizedTypeReference<List<MelhorEnvioLabelResponse>>() {}
            );

            log.info("Cart checkout completed successfully, {} orders processed",
                    response.getBody() != null ? response.getBody().size() : 0);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error checking out cart: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new MelhorEnvioException(
                    "Failed to checkout cart: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(),
                    "CHECKOUT_CART_ERROR"
            );
        } catch (Exception e) {
            log.error("Error checking out cart", e);
            throw new MelhorEnvioException("Error checking out cart: " + e.getMessage(), e);
        }
    }

    public MelhorEnvioGenerateResponse generateLabels(MelhorEnvioGenerateRequest request) {
        // TEMPORÁRIO - TESTES MELHOR ENVIO - URL hardcoded
        String url = URL_GENERATE;
        log.info("Generating labels for {} orders", request.orders().size());

        try {
            HttpEntity<MelhorEnvioGenerateRequest> entity = new HttpEntity<>(request, createHeaders());
            ResponseEntity<MelhorEnvioGenerateResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, MelhorEnvioGenerateResponse.class
            );

            log.info("Labels generated successfully");
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error generating labels: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new MelhorEnvioException(
                    "Failed to generate labels: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(),
                    "GENERATE_LABELS_ERROR"
            );
        } catch (Exception e) {
            log.error("Error generating labels", e);
            throw new MelhorEnvioException("Error generating labels: " + e.getMessage(), e);
        }
    }

    public void printLabels(MelhorEnvioPrintRequest request) {
        // TEMPORÁRIO - TESTES MELHOR ENVIO - URL hardcoded
        String url = URL_PRINT;
        log.info("Printing labels for {} orders", request.orders().size());

        try {
            HttpEntity<MelhorEnvioPrintRequest> entity = new HttpEntity<>(request, createHeaders());
            restTemplate.exchange(url, HttpMethod.POST, entity, Object.class);
            log.info("Labels sent to print successfully");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error printing labels: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new MelhorEnvioException(
                    "Failed to print labels: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(),
                    "PRINT_LABELS_ERROR"
            );
        } catch (Exception e) {
            log.error("Error printing labels", e);
            throw new MelhorEnvioException("Error printing labels: " + e.getMessage(), e);
        }
    }

    public void cancelLabel(String labelId) {
        // TEMPORÁRIO - TESTES MELHOR ENVIO - URL hardcoded
        String url = URL_SHIPMENT_PREFIX + labelId;
        log.info("Cancelling label {}", labelId);

        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            log.info("Label {} cancelled successfully", labelId);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error cancelling label: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new MelhorEnvioException(
                    "Failed to cancel label: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(),
                    "CANCEL_LABEL_ERROR"
            );
        } catch (Exception e) {
            log.error("Error cancelling label {}", labelId, e);
            throw new MelhorEnvioException("Error cancelling label: " + e.getMessage(), e);
        }
    }

    public MelhorEnvioTrackingResponse getTracking(String trackingId) {
        // TEMPORÁRIO - TESTES MELHOR ENVIO - URL hardcoded
        String url = URL_SHIPMENT_PREFIX + "tracking/" + trackingId;
        log.info("Getting tracking for {}", trackingId);

        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<MelhorEnvioTrackingResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, MelhorEnvioTrackingResponse.class
            );

            log.info("Tracking retrieved successfully for {}", trackingId);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error getting tracking: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new MelhorEnvioException(
                    "Failed to get tracking: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(),
                    "GET_TRACKING_ERROR"
            );
        } catch (Exception e) {
            log.error("Error getting tracking for {}", trackingId, e);
            throw new MelhorEnvioException("Error getting tracking: " + e.getMessage(), e);
        }
    }

    // TEMPORÁRIO - TESTES MELHOR ENVIO
    // REMOVER ANTES DE PRODUÇÃO
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(ACCESS_TOKEN);
        headers.set("User-Agent", USER_AGENT);
        return headers;
    }
    // FIM TEMPORÁRIO
}
