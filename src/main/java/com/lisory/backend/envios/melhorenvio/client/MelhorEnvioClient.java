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

@Component
public class MelhorEnvioClient {

    private static final Logger log = LoggerFactory.getLogger(MelhorEnvioClient.class);

    private final RestTemplate restTemplate;
    private final MelhorEnvioOAuthService oAuthService;
    private final MelhorEnvioProperties properties;

    public MelhorEnvioClient(
            RestTemplate defaultRestTemplate,
            MelhorEnvioOAuthService oAuthService,
            MelhorEnvioProperties properties
    ) {
        this.restTemplate = defaultRestTemplate;
        this.oAuthService = oAuthService;
        this.properties = properties;
    }

    public MelhorEnvioCalculateResponse calculateShipping(MelhorEnvioCalculateRequest request) {
        String url = properties.apiUrl() + "/api/v2/me/shipment/calculate";
        log.info("Calculating shipping from {} to {}", request.from().postalCode(), request.to().postalCode());

        try {
            HttpEntity<MelhorEnvioCalculateRequest> entity = new HttpEntity<>(request, createHeaders());
            ResponseEntity<MelhorEnvioCalculateResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, MelhorEnvioCalculateResponse.class
            );

            log.info("Shipping calculation completed successfully");
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
        String url = properties.apiUrl() + "/api/v2/me/cart";
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
        String url = properties.apiUrl() + "/api/v2/me/shipment/checkout";
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
        String url = properties.apiUrl() + "/api/v2/me/shipment/generate";
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
        String url = properties.apiUrl() + "/api/v2/me/shipment/print";
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
        String url = properties.apiUrl() + "/api/v2/me/shipment/" + labelId;
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
        String url = properties.apiUrl() + "/api/v2/me/shipment/tracking/" + trackingId;
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

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(oAuthService.getAccessToken());
        headers.set("User-Agent", properties.userAgent());
        return headers;
    }
}
