package com.enterprise.settlement.application.dto;

import java.math.BigDecimal;

public record ProcessSettlementCommand(
    String idempotencyKey,
    String debtorIban,
    String creditorIban,
    BigDecimal amount,
    String currency
) {}
