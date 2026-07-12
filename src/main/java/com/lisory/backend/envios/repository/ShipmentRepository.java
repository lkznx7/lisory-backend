package com.lisory.backend.envios.repository;

import com.lisory.backend.envios.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
    Optional<Shipment> findByOrderId(UUID orderId);
    Optional<Shipment> findByMelhorEnvioId(String melhorEnvioId);
}
