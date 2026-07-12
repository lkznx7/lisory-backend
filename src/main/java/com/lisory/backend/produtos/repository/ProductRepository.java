package com.lisory.backend.produtos.repository;

import com.lisory.backend.produtos.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findBySlug(String slug);
    Optional<Product> findBySku(String sku);
    boolean existsBySlug(String slug);
    boolean existsBySku(String sku);
    Page<Product> findByActiveTrue(Pageable pageable);
    Page<Product> findByCategoryIdAndActiveTrue(UUID categoryId, Pageable pageable);
    Page<Product> findByCollectionIdAndActiveTrue(UUID collectionId, Pageable pageable);
    Page<Product> findByFeaturedTrueAndActiveTrue(Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);
    List<Product> findByActiveTrueAndPriceBetween(BigDecimal min, BigDecimal max);
    List<Product> findTop12ByActiveTrueOrderByCreatedAtDesc();
}
