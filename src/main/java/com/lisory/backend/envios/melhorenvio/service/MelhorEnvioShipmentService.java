package com.lisory.backend.envios.melhorenvio.service;

import com.lisory.backend.config.properties.MelhorEnvioProperties;
import com.lisory.backend.envios.entity.Shipment;
import com.lisory.backend.envios.melhorenvio.dto.MelhorEnvioCartRequest;
import com.lisory.backend.envios.melhorenvio.dto.MelhorEnvioLabelResponse;
import com.lisory.backend.envios.repository.ShipmentRepository;
import com.lisory.backend.pedido.entity.Order;
import com.lisory.backend.pedido.entity.OrderItem;
import com.lisory.backend.pedido.repository.OrderRepository;
import com.lisory.backend.produtos.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MelhorEnvioShipmentService {

    private static final Logger log = LoggerFactory.getLogger(MelhorEnvioShipmentService.class);

    private static final Map<String, Integer> SERVICE_ID_MAP = Map.of(
            "PAC", 1,
            "SEDEX", 2
    );

    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final MelhorEnvioLabelService labelService;
    private final MelhorEnvioProperties properties;

    public MelhorEnvioShipmentService(
            OrderRepository orderRepository,
            ShipmentRepository shipmentRepository,
            MelhorEnvioLabelService labelService,
            MelhorEnvioProperties properties) {
        this.orderRepository = orderRepository;
        this.shipmentRepository = shipmentRepository;
        this.labelService = labelService;
        this.properties = properties;
    }

    public void generateShipment(UUID orderId) {
        log.info("[MELHOR_ENVIO_SHIPMENT] Starting shipment generation for order {}", orderId);

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("[MELHOR_ENVIO_SHIPMENT] Order not found: {}", orderId);
            return;
        }

        Shipment shipment = shipmentRepository.findByOrderId(orderId).orElse(null);
        if (shipment == null) {
            log.warn("[MELHOR_ENVIO_SHIPMENT] Shipment not found for order {}", orderId);
            return;
        }

        if (shipment.getMelhorEnvioId() != null) {
            log.info("[MELHOR_ENVIO_SHIPMENT] Shipment already generated for order {}: melhorEnvioId={}",
                    orderId, shipment.getMelhorEnvioId());
            return;
        }

        if (order.getAddress() == null) {
            log.error("[MELHOR_ENVIO_SHIPMENT] Order {} has no delivery address, cannot generate shipment", orderId);
            return;
        }

        if (order.getItems() == null || order.getItems().isEmpty()) {
            log.error("[MELHOR_ENVIO_SHIPMENT] Order {} has no items, cannot generate shipment", orderId);
            return;
        }

        try {
            int serviceId = mapServiceId(shipment.getService());
            MelhorEnvioCartRequest.AddressInfo from = buildFromAddress();
            MelhorEnvioCartRequest.AddressInfo to = buildToAddress(order);
            List<MelhorEnvioCartRequest.ProductInfo> products = buildProducts(order);
            List<MelhorEnvioCartRequest.VolumeInfo> volumes = buildVolumes(order);
            double insuranceValue = order.getTotal().doubleValue();
            String reminder = buildReminder(order);
            MelhorEnvioCartRequest.CartOptions options = new MelhorEnvioCartRequest.CartOptions(
                    insuranceValue, false, false, null, reminder);

            MelhorEnvioCartRequest cartRequest = new MelhorEnvioCartRequest(
                    serviceId, from, to, products, volumes, options);

            log.info("[MELHOR_ENVIO_SHIPMENT] Request built | serviceId={} from={} to={} products={} volumes={} insurance={}",
                    serviceId,
                    from.postalCode(),
                    to.postalCode(),
                    products.size(),
                    volumes.size(),
                    insuranceValue);
            log.info("[MELHOR_ENVIO_SHIPMENT] FROM: {} {} - {} {} - {}/{}",
                    from.address(), from.number(), from.district(), from.city(), from.stateAbbr(), from.postalCode());
            log.info("[MELHOR_ENVIO_SHIPMENT] TO: {} {} - {} {} - {}/{}",
                    to.address(), to.number(), to.district(), to.city(), to.stateAbbr(), to.postalCode());
            for (int i = 0; i < products.size(); i++) {
                MelhorEnvioCartRequest.ProductInfo p = products.get(i);
                log.info("[MELHOR_ENVIO_SHIPMENT] Product[{}]: name={} qty={} value={}", i, p.name(), p.quantity(), p.unitaryValue());
            }
            for (int i = 0; i < volumes.size(); i++) {
                MelhorEnvioCartRequest.VolumeInfo v = volumes.get(i);
                log.info("[MELHOR_ENVIO_SHIPMENT] Volume[{}]: {}x{}x{}cm {}kg", i, v.width(), v.height(), v.length(), v.weight());
            }

            MelhorEnvioLabelResponse cartResponse = labelService.addToCart(cartRequest);
            if (cartResponse == null) {
                log.error("[MELHOR_ENVIO_SHIPMENT] addToCart returned null for order {}", orderId);
                return;
            }
            log.info("[MELHOR_ENVIO_SHIPMENT] addToCart OK | id={} protocol={} status={}",
                    cartResponse.id(), cartResponse.protocol(), cartResponse.status());

            List<MelhorEnvioLabelResponse> checkoutOrders = labelService.checkout(List.of(cartResponse.id()));
            if (checkoutOrders == null || checkoutOrders.isEmpty()) {
                log.error("[MELHOR_ENVIO_SHIPMENT] checkout returned empty for order {}", orderId);
                return;
            }
            log.info("[MELHOR_ENVIO_SHIPMENT] checkout OK | {} orders", checkoutOrders.size());
            for (MelhorEnvioLabelResponse co : checkoutOrders) {
                log.info("[MELHOR_ENVIO_SHIPMENT]   checkout order: id={} protocol={} service={} status={}",
                        co.id(), co.protocol(), co.service(), co.status());
            }

            List<String> meOrderIds = checkoutOrders.stream()
                    .map(MelhorEnvioLabelResponse::id)
                    .toList();
            String pdfUrl = labelService.generate(meOrderIds, "private");
            log.info("[MELHOR_ENVIO_SHIPMENT] generate OK | pdfUrl={}", pdfUrl != null ? "present" : "null");

            shipment.setMelhorEnvioId(cartResponse.id());
            shipment.setProtocol(cartResponse.protocol());
            shipment.setCarrierCode(String.valueOf(serviceId));
            if (cartResponse.tracking() != null) {
                shipment.setTrackingCode(cartResponse.tracking());
            }
            if (cartResponse.trackingUrl() != null) {
                shipment.setTrackingUrl(cartResponse.trackingUrl());
            }
            if (pdfUrl != null) {
                shipment.setLabelUrl(pdfUrl);
            }
            if (cartResponse.status() != null) {
                shipment.setStatus(cartResponse.status());
            }
            shipmentRepository.save(shipment);

            log.info("[MELHOR_ENVIO_SHIPMENT] Shipment saved | orderId={} melhorEnvioId={} protocol={} tracking={} status={}",
                    orderId, shipment.getMelhorEnvioId(), shipment.getProtocol(),
                    shipment.getTrackingCode(), shipment.getStatus());

        } catch (Exception e) {
            log.error("[MELHOR_ENVIO_SHIPMENT] Failed to generate shipment for order {}: {}", orderId, e.getMessage(), e);
        }
    }

    private int mapServiceId(String serviceName) {
        if (serviceName == null || serviceName.isBlank()) return 1;
        String normalized = serviceName.trim().toUpperCase();
        return SERVICE_ID_MAP.getOrDefault(normalized, 1);
    }

    private MelhorEnvioCartRequest.AddressInfo buildFromAddress() {
        String cep = properties.originCep() != null
                ? properties.originCep().replaceAll("[^0-9]", "")
                : "";
        return new MelhorEnvioCartRequest.AddressInfo(
                properties.storeName() != null ? properties.storeName() : "Lisory",
                properties.storePhone() != null ? properties.storePhone() : "",
                properties.storeDocument() != null ? properties.storeDocument() : null,
                properties.storeEmail() != null ? properties.storeEmail() : "contato@lisory.com.br",
                properties.storeAddress() != null ? properties.storeAddress() : "",
                properties.storeComplement() != null ? properties.storeComplement() : "",
                properties.storeNumber() != null ? properties.storeNumber() : "",
                properties.storeNeighborhood() != null ? properties.storeNeighborhood() : "",
                properties.storeCity() != null ? properties.storeCity() : "",
                cep,
                properties.storeState() != null ? properties.storeState() : "",
                "BR",
                properties.storeDocument() != null ? properties.storeDocument() : null,
                properties.storeStateRegister() != null ? properties.storeStateRegister() : null,
                properties.storeCnae() != null ? properties.storeCnae() : null
        );
    }

    private MelhorEnvioCartRequest.AddressInfo buildToAddress(Order order) {
        com.lisory.backend.user.entity.Address addr = order.getAddress();
        String phone = order.getGuestPhone() != null ? order.getGuestPhone() : "";
        String cpf = order.getGuestCpf() != null ? order.getGuestCpf().replaceAll("[^0-9]", "") : null;
        String email = order.getGuestEmail() != null ? order.getGuestEmail() : "";
        String name = order.getGuestName() != null ? order.getGuestName() : "";
        String cep = addr.getZipCode() != null ? addr.getZipCode().replaceAll("[^0-9]", "") : "";

        return new MelhorEnvioCartRequest.AddressInfo(
                name,
                phone,
                cpf,
                email,
                addr.getStreet() != null ? addr.getStreet() : "",
                addr.getComplement() != null ? addr.getComplement() : "",
                addr.getNumber() != null ? addr.getNumber() : "",
                addr.getNeighborhood() != null ? addr.getNeighborhood() : "",
                addr.getCity() != null ? addr.getCity() : "",
                cep,
                addr.getState() != null ? addr.getState() : "",
                "BR",
                null,
                null,
                null
        );
    }

    private List<MelhorEnvioCartRequest.ProductInfo> buildProducts(Order order) {
        return order.getItems().stream().map(item -> {
            Product product = item.getProduct();
            BigDecimal unitPrice = product.getPromotionalPrice() != null
                    ? product.getPromotionalPrice() : product.getPrice();
            return new MelhorEnvioCartRequest.ProductInfo(
                    product.getName() != null ? product.getName() : "Produto",
                    String.valueOf(item.getQuantity()),
                    unitPrice != null ? unitPrice.toPlainString() : "0.00"
            );
        }).toList();
    }

    private List<MelhorEnvioCartRequest.VolumeInfo> buildVolumes(Order order) {
        int maxHeight = 0;
        int maxWidth = 0;
        int totalLength = 0;
        double totalWeightKg = 0;

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            int h = product.getHeight() != null ? Math.max(1, product.getHeight().intValue()) : 15;
            int w = product.getWidth() != null ? Math.max(1, product.getWidth().intValue()) : 10;
            int l = product.getLength() != null ? Math.max(1, product.getLength().intValue()) : 20;
            double weightKg = product.getWeight() != null ? product.getWeight().doubleValue() : 0.5;

            maxHeight = Math.max(maxHeight, h);
            maxWidth = Math.max(maxWidth, w);
            totalLength += l * item.getQuantity();
            totalWeightKg += weightKg * item.getQuantity();
        }

        if (maxHeight == 0) maxHeight = 15;
        if (maxWidth == 0) maxWidth = 10;
        if (totalLength == 0) totalLength = 20;
        if (totalWeightKg == 0) totalWeightKg = 0.5;

        return List.of(new MelhorEnvioCartRequest.VolumeInfo(maxHeight, maxWidth, totalLength, totalWeightKg));
    }

    private String buildReminder(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Lisory: ");
        List<String> itemsDesc = order.getItems().stream()
                .map(item -> item.getQuantity() + "x " + (item.getProduct().getName() != null ? item.getProduct().getName() : "Produto"))
                .toList();
        sb.append(String.join(", ", itemsDesc));
        String result = sb.toString();
        if (result.length() > 95) {
            result = result.substring(0, 92) + "...";
        }
        return result;
    }
}
