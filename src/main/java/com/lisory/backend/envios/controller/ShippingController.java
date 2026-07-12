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
            ShippingQuote quote = freightCalculator.calculate(request.zipCode(), freightItems);
            
            Map<String, Object> response = Map.of(
                "options", List.of(
                    Map.of(
                        "carrier", quote.carrier(),
                        "service", quote.service(),
                        "cost", quote.cost(),
                        "estimatedDays", quote.estimatedDays()
                    )
                )
            );

            logger.info("freight_calculation_completed", Map.of(
                "zipCode", request.zipCode(),
                "cost", quote.cost()
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("freight_calculation_error", Map.of("zipCode", request.zipCode()), e);
            
            Map<String, Object> fallback = Map.of(
                "options", List.of(
                    Map.of(
                        "carrier", "PAC",
                        "service", "PAC",
                        "cost", BigDecimal.ZERO,
                        "estimatedDays", 10
                    )
                )
            );
            return ResponseEntity.ok(fallback);
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
