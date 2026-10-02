package com.enterprise.settlement.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SettlementResponse(
    UUID transactionId,
    String idempotencyKey,
    String debtorIban,
    String creditorIban,
    BigDecimal amount,
    String currency,
    String status,
    Instant createdAt
) {}
