package com.lisory.backend.auth.repository;

import com.lisory.backend.auth.entity.AuthEntity;
import com.lisory.backend.auth.entity.ROLES;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthRepository extends JpaRepository<AuthEntity, UUID> {
    boolean existsByEmail(String email);
    Optional<AuthEntity> findByEmail(String email);
    Page<AuthEntity> findByRole(ROLES role, Pageable pageable);
    long countByRole(ROLES role);
    Page<AuthEntity> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}
