package com.lisory.backend.envios.services;

import com.lisory.backend.config.properties.MelhorEnvioProperties;
import com.lisory.backend.envios.melhorenvio.client.MelhorEnvioClient;
import com.lisory.backend.envios.melhorenvio.dto.MelhorEnvioCalculateRequest;
import com.lisory.backend.envios.melhorenvio.dto.MelhorEnvioCalculateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Component
public final class MelhorEnvioProvider implements ShippingProvider {

    private static final Logger log = LoggerFactory.getLogger(MelhorEnvioProvider.class);

    private static final BigDecimal FALLBACK_COST = new BigDecimal("20.00");
    private static final int FALLBACK_DAYS = 7;

    private final MelhorEnvioClient melhorEnvioClient;
    private final MelhorEnvioProperties properties;

    public MelhorEnvioProvider(MelhorEnvioClient melhorEnvioClient, MelhorEnvioProperties properties) {
        this.melhorEnvioClient = melhorEnvioClient;
        this.properties = properties;
    }

    @Override
    public ShippingQuote calculate(ShippingRequest request) {
        try {
            String originCep = properties.originCep().replaceAll("[^0-9]", "");
            String destCep = request.zipCode().replaceAll("[^0-9]", "");

            MelhorEnvioCalculateRequest apiRequest = new MelhorEnvioCalculateRequest(
                    new MelhorEnvioCalculateRequest.Address(originCep),
                    new MelhorEnvioCalculateRequest.Address(destCep),
                    List.of(new MelhorEnvioCalculateRequest.Product(
                            "1",
                            15, 10, 20,
                            request.weight() != null ? request.weight().doubleValue() : 0.5,
                            50.0,
                            request.productCount() != null ? request.productCount() : 1
                    )),
                    new MelhorEnvioCalculateRequest.Options(false, false),
                    null
            );

            MelhorEnvioCalculateResponse response = melhorEnvioClient.calculateShipping(apiRequest);

            if (response == null || response.options() == null || response.options().isEmpty()) {
                log.warn("melhor_envio_no_options_found for destCep={}, falling back", destCep);
                return fallback();
            }

            List<MelhorEnvioCalculateResponse.ShippingOption> validOptions = response.options().stream()
                    .filter(o -> o.error() == null || o.error().isBlank())
                    .toList();

            if (validOptions.isEmpty()) {
                log.warn("melhor_envio_all_options_errored for destCep={}", destCep);
                return fallback();
            }

            MelhorEnvioCalculateResponse.ShippingOption cheapest = validOptions.stream()
                    .min(Comparator.comparing(o -> new BigDecimal(o.price())))
                    .orElse(null);

            if (cheapest == null) {
                return fallback();
            }

            BigDecimal cost = new BigDecimal(cheapest.price());
            int days = cheapest.deliveryRange() != null ? cheapest.deliveryRange().max() : FALLBACK_DAYS;
            String carrier = cheapest.company() != null ? cheapest.company().name() : "UNKNOWN";
            String service = cheapest.name() != null ? cheapest.name() : "UNKNOWN";

            log.info("melhor_envio_quote carrier={} service={} cost={} days={}", carrier, service, cost, days);
            return new ShippingQuote(carrier, service, cost, days);
        } catch (Exception e) {
            log.error("melhor_envio_calculation_failed, falling back to default", e);
            return fallback();
        }
    }

    private ShippingQuote fallback() {
        return new ShippingQuote("PAC", "PAC", FALLBACK_COST, FALLBACK_DAYS);
    }
}
