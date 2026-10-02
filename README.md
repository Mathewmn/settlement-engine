# Settlement Engine

A Java 21 / Spring Boot 3.3 portfolio demonstration of transactional Outbox persistence, request idempotency and retryable Kafka event delivery.

This application records pending transactions. It does not transfer money, connect to a bank or establish SEPA / ISO 20022 compliance.

## Local setup

Requires Docker Compose. The local demonstration uses disposable credentials.

```sh
docker compose up --build
```

Application: http://localhost:8080. Prometheus: http://localhost:9090.

Alternatively, start only the infrastructure and run with JDK 21 and Maven 3.9:

```sh
docker compose up -d postgres kafka
mvn spring-boot:run
```

Flyway migrates an empty database. Hibernate validates the schema. Existing databases created with the original `ddl-auto: update` setup need a reviewed migration or a fresh disposable database. Do not reset a database containing data you need.

## Example request

```sh
curl -i http://localhost:8080/api/v1/settlements \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: example-001' \
  -d '{"debtorIban":"DE89370400440532013000","creditorIban":"DE02100500001099432211","amount":25.50,"currency":"EUR"}'
```

Repeating the same payload and key returns the same transaction ID. Changing the payload while reusing that key returns HTTP 409. Invalid money or a blank/oversized key returns HTTP 400. The POST returns 201 for both initial creation and an identical replay.

## Persistence and concurrency

The use case acquires a PostgreSQL transaction-scoped advisory lock based on the idempotency key before reading or creating a transaction. Concurrent requests with the same key wait for the owning transaction to finish. Hash collisions only serialize unrelated keys. A unique key constraint remains a safeguard.

The settlement row and Outbox row commit in one database transaction. `JpaOutboxWriter` records intent, while `OutboxPoller` performs delivery. The former unused Redis lock adapter and Redis dependency have been removed.

The poller selects at most 20 pending rows using `FOR UPDATE SKIP LOCKED`, waits for Kafka acknowledgement within the database transaction, then marks the event processed. Failed sends leave the event pending. The stable Outbox ID is sent in the Kafka `event-id` header.

**Delivery is at least once.** A process failure after broker acknowledgement but before database commit can redeliver an event. Consumers must deduplicate by event ID. No consumer or deduplication store is implemented. Transaction IDs are Kafka keys, not account IDs, so no per-account ordering guarantee is claimed. Synchronous sends hold database locks and are a deliberate demo simplification.

## Tests

With JDK 21 and Maven installed:

```sh
mvn test
mvn verify
```

`mvn test` runs unit and architecture checks. `mvn verify` also runs `SettlementIT` with real PostgreSQL and Kafka containers and requires Docker. The integration suite checks identical replay, eight concurrent same-key requests, one associated Outbox record and rollback on failed Outbox insertion. It does not assert end-to-end Kafka consumer delivery.

A GitHub Actions workflow is supplied, but its presence is not proof of a successful CI run. See `VERIFICATION.md` for the checks actually executed during this revision.

## Configuration and operations

`DB_URL`, `DB_USER`, `DB_PASSWORD` and `KAFKA_BOOTSTRAP_SERVERS` override local connection values. `/actuator/health` and `/actuator/prometheus` expose service health and standard metrics. The Prometheus config targets the Compose app service.

## Known limitations

No throughput/latency benchmark, coverage percentage, Grafana dashboard, tracing pipeline, DLQ, retry backoff or retention job is claimed. IBAN fields have basic required/length validation, not checksum or bank verification. Authentication, authorization, monetary/business rules, log privacy, consumer deduplication and operational hardening require further work before production use.
