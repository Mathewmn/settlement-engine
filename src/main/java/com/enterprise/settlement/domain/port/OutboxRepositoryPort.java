package com.enterprise.settlement.domain.port;

import java.util.UUID;

public interface OutboxRepositoryPort {
    void recordEvent(UUID aggregateId, String aggregateType, String eventType, String payload);
}
