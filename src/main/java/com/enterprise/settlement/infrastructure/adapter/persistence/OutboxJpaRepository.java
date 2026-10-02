package com.enterprise.settlement.infrastructure.adapter.persistence;
import com.enterprise.settlement.infrastructure.entity.OutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;
public interface OutboxJpaRepository extends JpaRepository<OutboxEntity, UUID> {
    @Query(value = "SELECT * FROM outbox_events WHERE processed = false ORDER BY created_at, id LIMIT 20 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<OutboxEntity> lockNextBatch();
}
