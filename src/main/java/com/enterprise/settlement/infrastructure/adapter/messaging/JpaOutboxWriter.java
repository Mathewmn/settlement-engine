package com.enterprise.settlement.infrastructure.adapter.messaging;

import com.enterprise.settlement.domain.port.OutboxRepositoryPort;
import com.enterprise.settlement.infrastructure.adapter.persistence.OutboxJpaRepository;
import com.enterprise.settlement.infrastructure.entity.OutboxEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JpaOutboxWriter implements OutboxRepositoryPort {

    private final OutboxJpaRepository outboxJpaRepository;

    public JpaOutboxWriter(OutboxJpaRepository outboxJpaRepository) {
        this.outboxJpaRepository = outboxJpaRepository;
    }

    @Override
    public void recordEvent(UUID aggregateId, String aggregateType, String eventType, String payload) {
        OutboxEntity entity = new OutboxEntity(UUID.randomUUID(), aggregateId, aggregateType, eventType, payload);
        outboxJpaRepository.save(entity);
    }
}
