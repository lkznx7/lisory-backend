package com.lisory.backend.envios.melhorenvio.repository;

import com.lisory.backend.envios.melhorenvio.entity.MelhorEnvioCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MelhorEnvioCredentialRepository extends JpaRepository<MelhorEnvioCredential, UUID> {
    Optional<MelhorEnvioCredential> findFirstByOrderByIdDesc();
}
