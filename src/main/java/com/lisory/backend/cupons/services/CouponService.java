package com.lisory.backend.cupons.services;

import com.lisory.backend.cupons.dto.CouponRequest;
import com.lisory.backend.cupons.dto.CouponResponse;
import com.lisory.backend.cupons.dto.CouponStatusResponse;
import com.lisory.backend.cupons.entity.Coupon;
import com.lisory.backend.cupons.repository.CouponRepository;
import com.lisory.backend.exception.BusinessException;
import com.lisory.backend.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Transactional
    public CouponResponse create(CouponRequest request) {
        if (couponRepository.existsByCode(request.code())) {
            throw new BusinessException("Coupon code already exists: " + request.code());
        }

        Coupon coupon = new Coupon();
        coupon.setCode(request.code());
        coupon.setDiscountType(request.discountType());
        coupon.setDiscountValue(request.discountValue());
        coupon.setMinOrderValue(request.minOrderValue());
        coupon.setMaxUses(request.maxUses());
        coupon.setUsedCount(0);
        coupon.setMaxUsesPerCustomer(request.maxUsesPerCustomer());
        coupon.setExpiresAt(request.expiresAt());
        coupon.setActive(request.active() != null ? request.active() : true);

        return toResponse(couponRepository.save(coupon));
    }

    @Transactional
    public CouponResponse update(UUID id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));

        if (!coupon.getCode().equals(request.code()) && couponRepository.existsByCode(request.code())) {
            throw new BusinessException("Coupon code already exists: " + request.code());
        }

        coupon.setCode(request.code());
        coupon.setDiscountType(request.discountType());
        coupon.setDiscountValue(request.discountValue());
        coupon.setMinOrderValue(request.minOrderValue());
        coupon.setMaxUses(request.maxUses());
        coupon.setMaxUsesPerCustomer(request.maxUsesPerCustomer());
        coupon.setExpiresAt(request.expiresAt());
        coupon.setActive(request.active() != null ? request.active() : coupon.getActive());

        return toResponse(couponRepository.save(coupon));
    }

    @Transactional
    public void delete(UUID id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        couponRepository.delete(coupon);
    }

    public CouponResponse findById(UUID id) {
        return toResponse(couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id)));
    }

    public Page<CouponResponse> findAll(String statusFilter, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Page<Coupon> couponPage = couponRepository.findAll(pageable);

        if (statusFilter != null && !statusFilter.isBlank()) {
            List<Coupon> filtered = switch (statusFilter.toLowerCase()) {
                case "active" -> couponPage.getContent().stream()
                        .filter(c -> c.getActive() &&
                                (c.getExpiresAt() == null || c.getExpiresAt().isAfter(now)) &&
                                (c.getMaxUses() == null || c.getUsedCount() < c.getMaxUses()))
                        .toList();
                case "expired" -> couponPage.getContent().stream()
                        .filter(c -> c.getExpiresAt() != null && c.getExpiresAt().isBefore(now))
                        .toList();
                case "exhausted" -> couponPage.getContent().stream()
                        .filter(c -> c.getMaxUses() != null && c.getUsedCount() >= c.getMaxUses())
                        .toList();
                case "inactive" -> couponPage.getContent().stream()
                        .filter(c -> !c.getActive())
                        .toList();
                default -> couponPage.getContent();
            };
            couponPage = new PageImpl<>(filtered, pageable, couponPage.getTotalElements());
        }

        return couponPage.map(this::toResponse);
    }

    public CouponResponse findByCode(String code) {
        return toResponse(couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code", code)));
    }

    @Transactional
    public CouponResponse toggleActive(UUID id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        coupon.setActive(!coupon.getActive());
        return toResponse(couponRepository.save(coupon));
    }

    @Transactional
    public CouponResponse duplicate(UUID id) {
        Coupon original = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));

        Coupon copy = new Coupon();
        copy.setCode(original.getCode() + "-COPY");
        copy.setDiscountType(original.getDiscountType());
        copy.setDiscountValue(original.getDiscountValue());
        copy.setMinOrderValue(original.getMinOrderValue());
        copy.setMaxUses(original.getMaxUses());
        copy.setUsedCount(0);
        copy.setMaxUsesPerCustomer(original.getMaxUsesPerCustomer());
        copy.setExpiresAt(original.getExpiresAt());
        copy.setActive(false);

        return toResponse(couponRepository.save(copy));
    }

    public CouponStatusResponse getStatus(UUID id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));

        LocalDateTime now = LocalDateTime.now();

        if (!coupon.getActive()) {
            return new CouponStatusResponse(coupon.getCode(), "inactive", "Coupon is inactive");
        }

        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(now)) {
            return new CouponStatusResponse(coupon.getCode(), "expired", "Coupon has expired");
        }

        if (coupon.getMaxUses() != null && coupon.getUsedCount() >= coupon.getMaxUses()) {
            return new CouponStatusResponse(coupon.getCode(), "exhausted", "Coupon has reached its maximum usage limit");
        }

        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isAfter(now)) {
            return new CouponStatusResponse(coupon.getCode(), "active", "Coupon is active and valid");
        }

        return new CouponStatusResponse(coupon.getCode(), "active", "Coupon is active and valid");
    }

    public Coupon validateAndApply(String code, BigDecimal orderValue, String customerEmail) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code", code));

        if (!coupon.getActive()) {
            throw new BusinessException("Coupon is not active");
        }

        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Coupon has expired");
        }

        if (coupon.getMaxUses() != null && coupon.getUsedCount() >= coupon.getMaxUses()) {
            throw new BusinessException("Coupon has reached its maximum usage limit");
        }

        if (coupon.getMinOrderValue() != null && orderValue.compareTo(coupon.getMinOrderValue()) < 0) {
            throw new BusinessException("Order value does not meet the minimum required value of " + coupon.getMinOrderValue());
        }

        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);

        return coupon;
    }

    private CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getMinOrderValue(),
                coupon.getMaxUses(),
                coupon.getUsedCount(),
                coupon.getMaxUsesPerCustomer(),
                coupon.getExpiresAt(),
                coupon.getActive(),
                coupon.getCreatedAt(),
                coupon.getUpdatedAt()
        );
    }
}
