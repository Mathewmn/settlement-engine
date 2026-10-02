package com.enterprise.settlement;

import com.enterprise.settlement.application.dto.ProcessSettlementCommand;
import com.enterprise.settlement.application.dto.SettlementResponse;
import com.enterprise.settlement.application.usecase.ProcessSettlementUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "settlement.outbox.enabled=false")
@Testcontainers
class SettlementIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private ProcessSettlementUseCase processSettlementUseCase;

    @Test
    @DisplayName("Should execute settlement transaction and maintain idempotency")
    void testSettlementProcessingAndIdempotency() {
        String idempotencyKey = "TEST-IDEMP-" + UUID.randomUUID();
        ProcessSettlementCommand cmd = new ProcessSettlementCommand(
            idempotencyKey,
            "DE89370400440532013000",
            "DE02100500001099432211",
            new BigDecimal("2500.50"),
            "EUR"
        );

        SettlementResponse firstCall = processSettlementUseCase.execute(cmd);
        assertThat(firstCall.transactionId()).isNotNull();
        assertThat(firstCall.status()).isEqualTo("PENDING");

        // Repeat call with identical idempotency key -> Must return same transaction without duplicate DB row
        SettlementResponse duplicateCall = processSettlementUseCase.execute(cmd);
        assertThat(duplicateCall.transactionId()).isEqualTo(firstCall.transactionId());
        assertThat(duplicateCall.idempotencyKey()).isEqualTo(idempotencyKey);
    }
    @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbc;

    @Test
    void concurrentDuplicateRequestsProduceOneTransactionAndOneEvent() throws Exception {
        String key = "concurrent-" + UUID.randomUUID();
        var cmd = new ProcessSettlementCommand(key, "DE89370400440532013000", "DE02100500001099432211", BigDecimal.TEN, "EUR");
        var start = new java.util.concurrent.CountDownLatch(1);
        var executor = java.util.concurrent.Executors.newFixedThreadPool(8);
        try {
            var futures = new java.util.ArrayList<java.util.concurrent.Future<SettlementResponse>>();
            for (int i = 0; i < 8; i++) futures.add(executor.submit(() -> { start.await(); return processSettlementUseCase.execute(cmd); }));
            start.countDown();
            var ids = new java.util.HashSet<UUID>();
            for (var future : futures) ids.add(future.get(30, java.util.concurrent.TimeUnit.SECONDS).transactionId());
            assertThat(ids).hasSize(1);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM settlement_transactions WHERE idempotency_key = ?", Integer.class, key)).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM outbox_events WHERE aggregate_id = ?", Integer.class, ids.iterator().next())).isEqualTo(1);
        } finally { executor.shutdownNow(); }
    }

    @Test
    void failedOutboxWriteRollsBackSettlement() {
        String key = "rollback-" + UUID.randomUUID();
        var cmd = new ProcessSettlementCommand(key, "debtor", "creditor", BigDecimal.TEN, "EUR");
        jdbc.execute("ALTER TABLE outbox_events ADD CONSTRAINT reject_test CHECK (aggregate_type <> 'SettlementTransaction')");
        try {
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> processSettlementUseCase.execute(cmd)).isInstanceOf(RuntimeException.class);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM settlement_transactions WHERE idempotency_key = ?", Integer.class, key)).isZero();
        } finally { jdbc.execute("ALTER TABLE outbox_events DROP CONSTRAINT reject_test"); }
    }

}
