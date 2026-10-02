package com.enterprise.settlement.domain.model;

import java.time.Instant;
import java.util.UUID;

public class SettlementTransaction {
    private final UUID transactionId;
    private final String idempotencyKey;
    private final String debtorIban;
    private final String creditorIban;
    private final Money money;
    private TransactionStatus status;
    private final Instant createdAt;

    public SettlementTransaction(String idempotencyKey, String debtorIban, String creditorIban, Money money) {
        this.transactionId = UUID.randomUUID();
        this.idempotencyKey = idempotencyKey;
        this.debtorIban = debtorIban;
        this.creditorIban = creditorIban;
        this.money = money;
        this.status = TransactionStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public SettlementTransaction(UUID transactionId, String idempotencyKey, String debtorIban, String creditorIban, Money money, TransactionStatus status, Instant createdAt) {
        this.transactionId = transactionId;
        this.idempotencyKey = idempotencyKey;
        this.debtorIban = debtorIban;
        this.creditorIban = creditorIban;
        this.money = money;
        this.status = status;
        this.createdAt = createdAt;
    }

    public void markSettled() {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction can only transition to SETTLED from PENDING status");
        }
        this.status = TransactionStatus.SETTLED;
    }

    public void markFailed() {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction can only fail if currently PENDING");
        }
        this.status = TransactionStatus.FAILED;
    }

    public UUID getTransactionId() { return transactionId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getDebtorIban() { return debtorIban; }
    public String getCreditorIban() { return creditorIban; }
    public Money getMoney() { return money; }
    public TransactionStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
