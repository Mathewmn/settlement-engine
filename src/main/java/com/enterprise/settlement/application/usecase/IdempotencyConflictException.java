package com.enterprise.settlement.application.usecase;
public class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException() { super("Idempotency key already belongs to a different request"); }
}
