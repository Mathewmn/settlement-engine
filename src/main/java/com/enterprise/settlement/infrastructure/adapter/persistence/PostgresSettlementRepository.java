package com.enterprise.settlement.infrastructure.adapter.persistence;

import com.enterprise.settlement.domain.model.SettlementTransaction;
import com.enterprise.settlement.domain.port.SettlementRepositoryPort;
import com.enterprise.settlement.infrastructure.entity.SettlementEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class PostgresSettlementRepository implements SettlementRepositoryPort {

    private final SettlementJpaRepository jpaRepository;

    public PostgresSettlementRepository(SettlementJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SettlementTransaction save(SettlementTransaction transaction) {
        SettlementEntity entity = SettlementEntity.fromDomain(transaction);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<SettlementTransaction> findById(UUID id) {
        return jpaRepository.findById(id).map(SettlementEntity::toDomain);
    }

    @Override
    public Optional<SettlementTransaction> findByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.findByIdempotencyKey(idempotencyKey).map(SettlementEntity::toDomain);
    }
}
