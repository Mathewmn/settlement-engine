# Revision verification — 2 October 2026

Executed in the review environment:
- Maven source/test compilation and `test`: PASS with `-Djava.version=17` on OpenJDK 17.
- 5 tests passed: Money validation (1), use-case replay/conflict/event creation (2), poller acknowledgement/failure (1), ArchUnit boundary (1).
- The test mock maker uses subclass mocking to avoid JVM self-attachment in restricted environments.

Not verified in this environment:
- The default Java 21 build. The sources compiled with the Java 17 compatibility override, while the project and Dockerfile still target Java 21.
- Docker image/Compose startup and Testcontainers integration suite. Docker is unavailable.
- PostgreSQL advisory-lock concurrency and schema migration at runtime. These have supplied integration tests but have not been executed here.
- End-to-end Kafka producer/consumer delivery, performance benchmarks or code coverage.
- The supplied GitHub Actions workflow has not run remotely.

Before external technical submission, run `mvn verify` with JDK 21 and Docker on a supported machine. Do not describe unexecuted integration checks as passing.
