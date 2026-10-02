package com.enterprise.settlement.infrastructure.entity;

import com.enterprise.settlement.domain.model.Money;
import com.enterprise.settlement.domain.model.SettlementTransaction;
import com.enterprise.settlement.domain.model.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "settlement_transactions", indexes = {
    @Index(name = "idx_tx_idempotency", columnList = "idempotency_key", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class SettlementEntity {

    @Id
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Column(nullable = false)
    private String debtorIban;

    @Column(nullable = false)
    private String creditorIban;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    public static SettlementEntity fromDomain(SettlementTransaction domain) {
        SettlementEntity entity = new SettlementEntity();
        entity.setId(domain.getTransactionId());
        entity.setIdempotencyKey(domain.getIdempotencyKey());
        entity.setDebtorIban(domain.getDebtorIban());
        entity.setCreditorIban(domain.getCreditorIban());
        entity.setAmount(domain.getMoney().amount());
        entity.setCurrency(domain.getMoney().currency());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    public SettlementTransaction toDomain() {
        return new SettlementTransaction(
            this.id,
            this.idempotencyKey,
            this.debtorIban,
            this.creditorIban,
            new Money(this.amount, this.currency),
            this.status,
            this.createdAt
        );
    }
}
