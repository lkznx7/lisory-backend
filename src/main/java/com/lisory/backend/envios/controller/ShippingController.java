package com.lisory.backend.envios.controller;

import com.lisory.backend.envios.services.FreightCalculator;
import com.lisory.backend.envios.services.ShippingQuote;
import com.lisory.backend.shared.log.StructuredLogger;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    private final FreightCalculator freightCalculator;
    private final StructuredLogger logger;

    public ShippingController(FreightCalculator freightCalculator) {
        this.freightCalculator = freightCalculator;
        this.logger = StructuredLogger.forClass(ShippingController.class);
    }

    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateFreight(
            @Valid @RequestBody FreightCalculationRequest request) {
        
        logger.info("freight_calculation_started", Map.of(
            "zipCode", request.zipCode(),
            "itemCount", request.items().size()
        ));

        List<FreightCalculator.FreightItem> freightItems = request.items().stream()
                .map(item -> new FreightCalculator.FreightItem(item.weight(), item.quantity()))
                .toList();

        try {
            List<ShippingQuote> quotes = freightCalculator.calculate(request.zipCode(), freightItems);
            
            List<Map<String, Object>> options = quotes.stream()
                    .map(quote -> Map.<String, Object>of(
                            "carrier", quote.carrier(),
                            "service", quote.service(),
                            "cost", quote.cost(),
                            "estimatedDays", quote.estimatedDays()
                    ))
                    .toList();

            Map<String, Object> response = Map.of("options", options);

            logger.info("freight_calculation_completed", Map.of(
                    "zipCode", request.zipCode(),
                    "optionCount", options.size()
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("freight_calculation_error", Map.of("zipCode", request.zipCode()), e);
            return ResponseEntity.internalServerError().body(Map.of("options", List.of()));
        }
    }

    public record FreightCalculationRequest(
            @NotBlank String zipCode,
            @NotNull @Valid List<FreightItemRequest> items
    ) {}

    public record FreightItemRequest(
            String productId,
            @NotNull Integer quantity,
            @NotNull BigDecimal weight,
            BigDecimal width,
            BigDecimal height,
            BigDecimal length
    ) {}
}
