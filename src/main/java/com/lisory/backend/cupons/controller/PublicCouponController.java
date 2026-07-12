package com.lisory.backend.cupons.controller;

import com.lisory.backend.cupons.dto.CouponResponse;
import com.lisory.backend.cupons.services.CouponService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/coupons")
public class PublicCouponController {

    private final CouponService couponService;

    public PublicCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping("/validate")
    public ResponseEntity<CouponResponse> validate(@Valid @RequestBody ValidateCouponRequest request) {
        couponService.validateAndApply(request.code(), request.orderValue(), null);
        return ResponseEntity.ok(couponService.findByCode(request.code()));
    }

    record ValidateCouponRequest(@NotBlank String code, @NotNull BigDecimal orderValue) {}
}
