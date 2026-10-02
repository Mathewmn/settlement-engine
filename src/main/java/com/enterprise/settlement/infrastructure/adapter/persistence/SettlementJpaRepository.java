package com.enterprise.settlement.infrastructure.adapter.persistence;

import com.enterprise.settlement.infrastructure.entity.SettlementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SettlementJpaRepository extends JpaRepository<SettlementEntity, UUID> {
    Optional<SettlementEntity> findByIdempotencyKey(String idempotencyKey);
}
