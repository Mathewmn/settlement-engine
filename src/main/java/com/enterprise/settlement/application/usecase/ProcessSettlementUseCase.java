package com.enterprise.settlement.application.usecase;

import com.enterprise.settlement.application.dto.ProcessSettlementCommand;
import com.enterprise.settlement.application.dto.SettlementResponse;
import com.enterprise.settlement.domain.model.Money;
import com.enterprise.settlement.domain.model.SettlementTransaction;
import com.enterprise.settlement.domain.port.OutboxRepositoryPort;
import com.enterprise.settlement.domain.port.SettlementRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import com.enterprise.settlement.domain.port.IdempotencyLockPort;

@Service
public class ProcessSettlementUseCase {

    private final SettlementRepositoryPort settlementRepository;
    private final OutboxRepositoryPort outboxRepository;
    private final ObjectMapper objectMapper;
    private final IdempotencyLockPort idempotencyLock;

    public ProcessSettlementUseCase(SettlementRepositoryPort settlementRepository,
                                  OutboxRepositoryPort outboxRepository,
                                  ObjectMapper objectMapper, IdempotencyLockPort idempotencyLock) {
        this.settlementRepository = settlementRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.idempotencyLock = idempotencyLock;
    }

    @Transactional
    public SettlementResponse execute(ProcessSettlementCommand cmd) {
        if (cmd.idempotencyKey() == null || cmd.idempotencyKey().isBlank() || cmd.idempotencyKey().length() > 128)
            throw new IllegalArgumentException("Idempotency-Key must contain 1 to 128 characters");
        Money money = new Money(cmd.amount(), cmd.currency());
        if (cmd.debtorIban() == null || cmd.debtorIban().isBlank() || cmd.creditorIban() == null || cmd.creditorIban().isBlank())
            throw new IllegalArgumentException("Both account identifiers are required");
        idempotencyLock.lock(cmd.idempotencyKey());
        var existing = settlementRepository.findByIdempotencyKey(cmd.idempotencyKey());
        if (existing.isPresent()) {
            var tx = existing.get();
            if (!tx.getDebtorIban().equals(cmd.debtorIban()) || !tx.getCreditorIban().equals(cmd.creditorIban()) ||
                tx.getMoney().amount().compareTo(money.amount()) != 0 || !tx.getMoney().currency().equals(money.currency()))
                throw new IdempotencyConflictException();
            return new SettlementResponse(
                tx.getTransactionId(), tx.getIdempotencyKey(),
                tx.getDebtorIban(), tx.getCreditorIban(),
                tx.getMoney().amount(), tx.getMoney().currency(),
                tx.getStatus().name(), tx.getCreatedAt()
            );
        }

        SettlementTransaction transaction = new SettlementTransaction(
            cmd.idempotencyKey(),
            cmd.debtorIban(),
            cmd.creditorIban(),
            money
        );

        SettlementTransaction saved = settlementRepository.save(transaction);

        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                "transactionId", saved.getTransactionId().toString(),
                "debtorIban", saved.getDebtorIban(),
                "creditorIban", saved.getCreditorIban(),
                "amount", saved.getMoney().amount(),
                "currency", saved.getMoney().currency(),
                "status", saved.getStatus().name()
            ));

            outboxRepository.recordEvent(
                saved.getTransactionId(),
                "SettlementTransaction",
                "SETTLEMENT_INITIATED",
                payload
            );
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize transaction payload for outbox", e);
        }

        return new SettlementResponse(
            saved.getTransactionId(), saved.getIdempotencyKey(),
            saved.getDebtorIban(), saved.getCreditorIban(),
            saved.getMoney().amount(), saved.getMoney().currency(),
            saved.getStatus().name(), saved.getCreatedAt()
        );
    }
}
