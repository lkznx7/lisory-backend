package com.lisory.backend.envios.melhorenvio.service;

import com.lisory.backend.envios.melhorenvio.client.MelhorEnvioClient;
import com.lisory.backend.envios.melhorenvio.dto.*;
import com.lisory.backend.envios.services.ShippingQuote;
import com.lisory.backend.produtos.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MelhorEnvioShippingService {

    private static final Logger log = LoggerFactory.getLogger(MelhorEnvioShippingService.class);

    private final MelhorEnvioClient client;

    public MelhorEnvioShippingService(MelhorEnvioClient client) {
        this.client = client;
    }

    public record ShippingItem(
            String productId,
            String productName,
            int quantity,
            double weight,
            double width,
            double height,
            double length,
            BigDecimal insuranceValue
    ) {}

    public List<ShippingQuote> calculateShipping(
            String originCep,
            String destinationCep,
            List<ShippingItem> items,
            BigDecimal insuranceValue
    ) {
        log.info("Calculating shipping from {} to {} with {} items", originCep, destinationCep, items.size());

        List<MelhorEnvioCalculateRequest.Product> products = items.stream()
                .map(item -> new MelhorEnvioCalculateRequest.Product(
                        item.productId(),
                        item.width(),
                        item.height(),
                        item.length(),
                        item.weight(),
                        item.insuranceValue() != null ? item.insuranceValue().doubleValue() : 0.0,
                        item.quantity()
                ))
                .toList();

        MelhorEnvioCalculateRequest request = new MelhorEnvioCalculateRequest(
                new MelhorEnvioCalculateRequest.Address(originCep),
                new MelhorEnvioCalculateRequest.Address(destinationCep),
                products,
                new MelhorEnvioCalculateRequest.Options(false, false),
                null
        );

        MelhorEnvioCalculateResponse response = client.calculateShipping(request);

        if (response == null || response.options() == null) {
            log.warn("No shipping options returned from Melhor Envio");
            return List.of();
        }

        List<ShippingQuote> quotes = new ArrayList<>();

        for (MelhorEnvioCalculateResponse.ShippingOption option : response.options()) {
            if (option.error() != null && !option.error().isBlank()) {
                log.warn("Shipping option {} has error: {}", option.name(), option.error());
                continue;
            }

            int estimatedDays = 0;
            if (option.deliveryRange() != null) {
                estimatedDays = option.deliveryRange().max();
            } else if (option.days() != null) {
                try {
                    estimatedDays = Integer.parseInt(option.days());
                } catch (NumberFormatException ignored) {}
            }

            String companyName = option.company() != null ? option.company().name() : "Unknown";

            BigDecimal price = BigDecimal.ZERO;
            if (option.price() != null) {
                try {
                    price = new BigDecimal(option.price());
                } catch (NumberFormatException ignored) {}
            }

            quotes.add(new ShippingQuote(
                    companyName,
                    option.name(),
                    price,
                    estimatedDays
            ));
        }

        log.info("Found {} valid shipping options", quotes.size());
        return quotes;
    }

    public MelhorEnvioLabelResponse buyLabel(int serviceId, MelhorEnvioCartRequest request) {
        log.info("Buying label for service {}", serviceId);
        MelhorEnvioLabelResponse response = client.addToCart(request);
        log.info("Label added to cart with protocol: {}", response != null ? response.protocol() : "N/A");
        return response;
    }

    public List<MelhorEnvioLabelResponse> checkoutCart(int serviceId, double insuranceValue) {
        log.info("Checking out cart with service {}", serviceId);
        MelhorEnvioCheckoutRequest request = new MelhorEnvioCheckoutRequest(serviceId, insuranceValue, true);
        List<MelhorEnvioLabelResponse> response = client.checkoutCart(request);
        log.info("Cart checkout completed, {} orders processed", response != null ? response.size() : 0);
        return response;
    }

    public String generateLabels(List<String> orderIds) {
        log.info("Generating labels for {} orders", orderIds.size());
        MelhorEnvioGenerateRequest request = new MelhorEnvioGenerateRequest(orderIds, "private");
        MelhorEnvioGenerateResponse response = client.generateLabels(request);
        return response != null ? response.url() : null;
    }

    public void printLabels(List<String> orderIds) {
        log.info("Printing labels for {} orders", orderIds.size());
        MelhorEnvioPrintRequest request = new MelhorEnvioPrintRequest(orderIds, "public");
        client.printLabels(request);
        log.info("Labels sent to print");
    }

    public void cancelShipment(String labelId) {
        log.info("Cancelling shipment with label {}", labelId);
        client.cancelLabel(labelId);
        log.info("Shipment {} cancelled successfully", labelId);
    }

    public MelhorEnvioTrackingResponse trackShipment(String trackingId) {
        log.info("Tracking shipment with id {}", trackingId);
        MelhorEnvioTrackingResponse response = client.getTracking(trackingId);
        log.info("Shipment {} status: {}", trackingId, response != null ? response.status() : "UNKNOWN");
        return response;
    }
}
