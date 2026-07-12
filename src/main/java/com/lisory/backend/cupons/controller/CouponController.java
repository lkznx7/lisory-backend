package com.lisory.backend.cupons.controller;

import com.lisory.backend.cupons.dto.CouponRequest;
import com.lisory.backend.cupons.dto.CouponResponse;
import com.lisory.backend.cupons.dto.CouponStatusResponse;
import com.lisory.backend.cupons.services.CouponService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    public ResponseEntity<CouponResponse> create(@Valid @RequestBody CouponRequest request) {
        return ResponseEntity.ok(couponService.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<CouponResponse>> findAll(
            @RequestParam(required = false) String status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(couponService.findAll(status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(couponService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CouponResponse> update(@PathVariable UUID id, @Valid @RequestBody CouponRequest request) {
        return ResponseEntity.ok(couponService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        couponService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<CouponResponse> toggleActive(@PathVariable UUID id) {
        return ResponseEntity.ok(couponService.toggleActive(id));
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<CouponResponse> duplicate(@PathVariable UUID id) {
        return ResponseEntity.ok(couponService.duplicate(id));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<CouponStatusResponse> getStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(couponService.getStatus(id));
    }
}
