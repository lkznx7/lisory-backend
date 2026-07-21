package com.lisory.backend.envios.melhorenvio.service;

import com.lisory.backend.envios.melhorenvio.client.MelhorEnvioClient;
import com.lisory.backend.envios.melhorenvio.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MelhorEnvioLabelService {

    private static final Logger log = LoggerFactory.getLogger(MelhorEnvioLabelService.class);

    private final MelhorEnvioClient client;

    public MelhorEnvioLabelService(MelhorEnvioClient client) {
        this.client = client;
    }

    public MelhorEnvioLabelResponse addToCart(MelhorEnvioCartRequest request) {
        log.info("Adding shipment to cart for service {}", request.service());
        MelhorEnvioLabelResponse response = client.addToCart(request);
        log.info("Shipment added to cart, protocol: {}", response != null ? response.protocol() : "N/A");
        return response;
    }

    public List<MelhorEnvioLabelResponse> checkout(List<String> orderIds) {
        log.info("Checking out cart for orders {}", orderIds);
        MelhorEnvioCheckoutRequest request = new MelhorEnvioCheckoutRequest(orderIds);
        List<MelhorEnvioLabelResponse> response = client.checkoutCart(request);
        log.info("Cart checkout completed, {} orders", response != null ? response.size() : 0);
        return response;
    }

    public String generate(List<String> orderIds, String mode) {
        log.info("Generating labels for {} orders in {} mode", orderIds.size(), mode);
        MelhorEnvioGenerateRequest request = new MelhorEnvioGenerateRequest(orderIds, mode);
        client.generateLabels(request);

        // Aguardar o processamento assíncrono do Melhor Envio
        try {
            log.info("Waiting 2 seconds for label generation processing...");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for label generation", e);
        }

        MelhorEnvioPrintRequest printRequest = new MelhorEnvioPrintRequest(orderIds, mode);
        MelhorEnvioPrintResponse printResponse = client.printLabels(printRequest);
        String url = printResponse != null ? printResponse.url() : null;
        log.info("Labels generated, PDF URL: {}", url != null ? "present" : "null");
        return url;
    }

    public MelhorEnvioPrintResponse print(List<String> orderIds, String mode) {
        log.info("Printing labels for {} orders in {} mode", orderIds.size(), mode);
        MelhorEnvioPrintRequest request = new MelhorEnvioPrintRequest(orderIds, mode);
        MelhorEnvioPrintResponse response = client.printLabels(request);
        log.info("Labels print URL retrieved: {}", response != null ? response.url() : "null");
        return response;
    }

    public void cancel(String labelId) {
        log.info("Cancelling label {}", labelId);
        client.cancelLabel(labelId);
        log.info("Label {} cancelled", labelId);
    }

    public MelhorEnvioLabelResponse buyAndGenerate(MelhorEnvioCartRequest request) {
        log.info("Full label flow: addToCart -> checkout -> generate for service {}", request.service());

        MelhorEnvioLabelResponse cartResponse = addToCart(request);
        if (cartResponse == null) {
            throw new RuntimeException("Failed to add shipment to cart");
        }

        List<MelhorEnvioLabelResponse> checkoutOrders = checkout(List.of(cartResponse.id()));
        if (checkoutOrders == null || checkoutOrders.isEmpty()) {
            throw new RuntimeException("Failed to checkout cart");
        }

        List<String> orderIds = checkoutOrders.stream()
                .map(MelhorEnvioLabelResponse::id)
                .toList();

        String pdfUrl = generate(orderIds, "private");
        log.info("Full label flow completed, PDF URL: {}", pdfUrl != null ? "present" : "null");

        return cartResponse;
    }
}
