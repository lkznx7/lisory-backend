package com.lisory.backend.pedido.repository;

import com.lisory.backend.pedido.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByIdAndUserId(UUID id, UUID userId);
    Page<Order> findByUserId(UUID userId, Pageable pageable);
    Page<Order> findByStatus(String status, Pageable pageable);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status = :status")
    BigDecimal sumTotalByStatus(@Param("status") String status);
}
