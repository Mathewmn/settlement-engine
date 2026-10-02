package com.enterprise.settlement.domain.port;
public interface IdempotencyLockPort {
    void lock(String idempotencyKey);
}
