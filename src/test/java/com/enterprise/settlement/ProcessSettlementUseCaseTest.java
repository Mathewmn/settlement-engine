package com.enterprise.settlement;
import com.enterprise.settlement.application.dto.ProcessSettlementCommand;
import com.enterprise.settlement.application.usecase.*;
import com.enterprise.settlement.domain.model.*;
import com.enterprise.settlement.domain.port.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class ProcessSettlementUseCaseTest {
 @Test void replayDoesNotCreateAnotherEventAndConflictingRequestFails() {
  var repository = mock(SettlementRepositoryPort.class);
  var outbox = mock(OutboxRepositoryPort.class);
  var lock = mock(IdempotencyLockPort.class);
  var tx = new SettlementTransaction("key", "debtor", "creditor", new Money(new BigDecimal("10.00"), "EUR"));
  when(repository.findByIdempotencyKey("key")).thenReturn(Optional.of(tx));
  var service = new ProcessSettlementUseCase(repository, outbox, new ObjectMapper(), lock);
  var cmd = new ProcessSettlementCommand("key", "debtor", "creditor", new BigDecimal("10"), "EUR");
  assertThat(service.execute(cmd).transactionId()).isEqualTo(tx.getTransactionId());
  var order = inOrder(lock, repository);
  order.verify(lock).lock("key"); order.verify(repository).findByIdempotencyKey("key");
  verifyNoInteractions(outbox);
  assertThatThrownBy(() -> service.execute(new ProcessSettlementCommand("key", "debtor", "creditor", new BigDecimal("11"), "EUR")))
    .isInstanceOf(IdempotencyConflictException.class);
  verify(repository, never()).save(any());
 }
 @Test void newRequestWritesSettlementAndOutbox() {
  var repository = mock(SettlementRepositoryPort.class);
  var outbox = mock(OutboxRepositoryPort.class);
  when(repository.findByIdempotencyKey("key")).thenReturn(Optional.empty());
  when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
  var service = new ProcessSettlementUseCase(repository, outbox, new ObjectMapper(), mock(IdempotencyLockPort.class));
  var result = service.execute(new ProcessSettlementCommand("key", "debtor", "creditor", BigDecimal.TEN, "EUR"));
  verify(outbox).recordEvent(eq(result.transactionId()), eq("SettlementTransaction"), eq("SETTLEMENT_INITIATED"), contains(result.transactionId().toString()));
 }
}
