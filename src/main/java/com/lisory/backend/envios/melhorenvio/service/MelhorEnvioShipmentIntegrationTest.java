package com.lisory.backend.envios.melhorenvio.service;

import com.lisory.backend.envios.melhorenvio.client.MelhorEnvioClient;
import com.lisory.backend.envios.melhorenvio.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MelhorEnvioShipmentIntegrationTest implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MelhorEnvioShipmentIntegrationTest.class);

    private final MelhorEnvioClient client;

    public MelhorEnvioShipmentIntegrationTest(MelhorEnvioClient client) {
        this.client = client;
    }

    @Override
    public void run(String... args) {
        if (args.length == 0 || !"test-melhor-envio-shipment".equals(args[0])) {
            return;
        }

        log.info("========================================");
        log.info("MELHOR ENVIO SHIPMENT INTEGRATION TEST");
        log.info("========================================");

        try {
            int serviceId = 1; // PAC

            MelhorEnvioCartRequest.AddressInfo from = new MelhorEnvioCartRequest.AddressInfo(
                    "Lisory", "(11) 99999-9999", null, "contato@lisory.com.br",
                    "Av. Paulista", "Sala 1", "1000",
                    "Bela Vista", "São Paulo", "01310100", "SP", "BR",
                    null, null, null
            );

            MelhorEnvioCartRequest.AddressInfo to = new MelhorEnvioCartRequest.AddressInfo(
                    "Cliente Teste", "(11) 98888-8888", null, "cliente@teste.com",
                    "Rua Augusta", "500", "",
                    "Consolação", "São Paulo", "01305100", "SP", "BR",
                    null, null, null
            );

            List<MelhorEnvioCartRequest.ProductInfo> products = List.of(
                    new MelhorEnvioCartRequest.ProductInfo("Produto Teste", "1", "99.90")
            );

            List<MelhorEnvioCartRequest.VolumeInfo> volumes = List.of(
                    new MelhorEnvioCartRequest.VolumeInfo(10, 15, 20, 500)
            );

            MelhorEnvioCartRequest.CartOptions options = new MelhorEnvioCartRequest.CartOptions(
                    99.90, false, false, null, "Lisory: 1x Produto Teste"
            );

            MelhorEnvioCartRequest cartRequest = new MelhorEnvioCartRequest(
                    serviceId, from, to, products, volumes, options
            );

            log.info("STEP 1: addToCart");
            log.info("Request: service={} from.postalCode={} to.postalCode={} products={} volumes={}",
                    serviceId, from.postalCode(), to.postalCode(), products.size(), volumes.size());

            MelhorEnvioLabelResponse cartResponse = client.addToCart(cartRequest);
            log.info("Response: id={} protocol={} service={} status={}",
                    cartResponse.id(), cartResponse.protocol(), cartResponse.service(), cartResponse.status());

            log.info("STEP 2: checkoutCart");
            MelhorEnvioCheckoutRequest checkoutRequest = new MelhorEnvioCheckoutRequest(List.of(cartResponse.id()));
            List<MelhorEnvioLabelResponse> checkoutResponse = client.checkoutCart(checkoutRequest);
            log.info("Response: {} orders", checkoutResponse.size());
            for (MelhorEnvioLabelResponse r : checkoutResponse) {
                log.info("  Order: id={} protocol={} service={} status={}", r.id(), r.protocol(), r.service(), r.status());
            }

            List<String> meOrderIds = checkoutResponse.stream().map(MelhorEnvioLabelResponse::id).toList();

            log.info("STEP 3: generateLabels");
            MelhorEnvioGenerateRequest generateRequest = new MelhorEnvioGenerateRequest(meOrderIds, "private");
            MelhorEnvioGenerateResponse generateResponse = client.generateLabels(generateRequest);
            log.info("Response: url={}", generateResponse != null ? generateResponse.url() : "null");

            log.info("========================================");
            log.info("TEST COMPLETED SUCCESSFULLY");
            log.info("Shipment ID: {}", cartResponse.id());
            log.info("Protocol: {}", cartResponse.protocol());
            log.info("Label URL: {}", generateResponse != null ? generateResponse.url() : "N/A");
            log.info("========================================");

        } catch (Exception e) {
            log.error("TEST FAILED: {}", e.getMessage(), e);
        }
    }
}
