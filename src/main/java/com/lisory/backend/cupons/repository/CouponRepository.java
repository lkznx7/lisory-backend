package com.lisory.backend.cupons.repository;

import com.lisory.backend.cupons.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    Optional<Coupon> findByCode(String code);
    boolean existsByCode(String code);
    List<Coupon> findByActiveTrue();
}
