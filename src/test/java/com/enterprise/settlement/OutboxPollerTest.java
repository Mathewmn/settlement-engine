package com.enterprise.settlement;
import com.enterprise.settlement.infrastructure.adapter.messaging.OutboxPoller;
import com.enterprise.settlement.infrastructure.adapter.persistence.OutboxJpaRepository;
import com.enterprise.settlement.infrastructure.entity.OutboxEntity;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class OutboxPollerTest {
 @Test @SuppressWarnings({"unchecked", "rawtypes"})
 void acknowledgementMarksProcessedAndFailureRemainsRetryable() {
  var repository = mock(OutboxJpaRepository.class);
  KafkaTemplate<String, String> kafka = mock(KafkaTemplate.class);
  var event = new OutboxEntity(UUID.randomUUID(), UUID.randomUUID(), "SettlementTransaction", "SETTLEMENT_INITIATED", "{}");
  when(repository.lockNextBatch()).thenReturn(List.of(event));
  when(kafka.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.<SendResult<String, String>>failedFuture(new RuntimeException("broker offline")));
  var poller = new OutboxPoller(repository, kafka);
  poller.dispatchOutboxMessages();
  assertThat(event.isProcessed()).isFalse(); verify(repository, never()).save(any());
  when(kafka.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(null));
  poller.dispatchOutboxMessages();
  assertThat(event.isProcessed()).isTrue(); verify(repository).save(event);
  ArgumentCaptor<ProducerRecord> record = ArgumentCaptor.forClass(ProducerRecord.class);
  verify(kafka, times(2)).send(record.capture());
  assertThat(new String(record.getValue().headers().lastHeader("event-id").value(), java.nio.charset.StandardCharsets.UTF_8)).isEqualTo(event.getId().toString());
 }
}
