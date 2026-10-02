# Transaction and delivery decisions

## One database transaction for record and event intent

The use case saves the pending settlement and Outbox row in one PostgreSQL transaction. This keeps local state consistent without requiring an atomic transaction across PostgreSQL and Kafka. The scheduled publisher operates after commit.

## Same-key concurrency

A PostgreSQL advisory transaction lock serializes processing for a request key before the lookup. The transaction lifetime releases the lock automatically. The unique constraint remains a safeguard. A replay compares accounts, amount and currency and returns the existing transaction only when they match. Conflicting payloads return HTTP 409.

Hash collisions can serialize unrelated requests. Lock contention and transaction time need observation under a measured workload. The current integration suite is supplied but has not been executed in the revision environment.

## At-least-once publication

The publisher selects a bounded batch using FOR UPDATE SKIP LOCKED. It waits for Kafka acknowledgement while holding the database transaction and then marks the row processed. Failed publication leaves the row pending. The stable event-id header supports downstream deduplication.

A crash between broker acknowledgement and database commit can cause redelivery. A durable consumer deduplication store is required and is not included. Synchronous sends hold locks and occupy a database connection, so this approach needs load testing before production deployment.

## Extension points

Authentication, durable consumer deduplication, retry backoff, event retention and business validation remain separate tasks. The project does not implement bank transfer execution or certify financial compliance.
