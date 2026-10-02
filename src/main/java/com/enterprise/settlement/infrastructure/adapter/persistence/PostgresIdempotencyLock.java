package com.enterprise.settlement.infrastructure.adapter.persistence;
import com.enterprise.settlement.domain.port.IdempotencyLockPort;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
@Component
public class PostgresIdempotencyLock implements IdempotencyLockPort {
    private final EntityManager entityManager;
    public PostgresIdempotencyLock(EntityManager entityManager) { this.entityManager = entityManager; }
    @Override
    public void lock(String key) {
        // Lock lasts until transaction completion. Hash collisions only serialize unrelated keys.
        entityManager.createNativeQuery("SELECT 1 FROM pg_advisory_xact_lock(hashtextextended(CAST(:key AS text), 0))")
            .setParameter("key", key).getSingleResult();
    }
}
