package com.lisory.backend.produtos.repository;

import com.lisory.backend.produtos.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollectionRepository extends JpaRepository<Collection, UUID> {
    Optional<Collection> findBySlug(String slug);
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
    List<Collection> findByActiveTrue();
}
