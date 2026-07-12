package com.lisory.backend.carrinho.repository;

import com.lisory.backend.carrinho.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUserId(UUID userId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.product LEFT JOIN FETCH i.product.images WHERE c.guestCartId = :guestCartId")
    Optional<Cart> findByGuestCartIdWithItems(@Param("guestCartId") UUID guestCartId);

    Optional<Cart> findByGuestCartId(UUID guestCartId);
}
