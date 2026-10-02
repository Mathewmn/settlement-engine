package com.enterprise.settlement.infrastructure.adapter.messaging;
import com.enterprise.settlement.infrastructure.adapter.persistence.OutboxJpaRepository;
import com.enterprise.settlement.infrastructure.entity.OutboxEntity;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
@Component
@ConditionalOnProperty(name = "settlement.outbox.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPoller {
    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);
    private final OutboxJpaRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    public OutboxPoller(OutboxJpaRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }
    @Scheduled(fixedDelayString = "${settlement.outbox.delay-ms:500}")
    @Transactional
    public void dispatchOutboxMessages() {
        // Synchronous acknowledgement keeps row locks and DB updates in this transaction.
        // Kafka ACK followed by process failure can still redeliver: consumers must deduplicate event-id.
        for (OutboxEntity event : outboxRepository.lockNextBatch()) {
            ProducerRecord<String, String> record = new ProducerRecord<>("settlement.events.v1", event.getAggregateId().toString(), event.getPayload());
            record.headers().add("event-id", event.getId().toString().getBytes(StandardCharsets.UTF_8));
            try {
                kafkaTemplate.send(record).get(20, TimeUnit.SECONDS);
                event.setProcessed(true);
                outboxRepository.save(event);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.warn("Outbox interrupted, event {} remains pending", event.getId());
                break;
            } catch (Exception ex) {
                log.warn("Outbox delivery failed, event {} remains pending", event.getId(), ex);
                break;
            }
        }
    }
}
