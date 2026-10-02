package com.enterprise.settlement.domain.port;

import com.enterprise.settlement.domain.model.SettlementTransaction;
import java.util.Optional;
import java.util.UUID;

public interface SettlementRepositoryPort {
    SettlementTransaction save(SettlementTransaction transaction);
    Optional<SettlementTransaction> findById(UUID id);
    Optional<SettlementTransaction> findByIdempotencyKey(String idempotencyKey);
}
