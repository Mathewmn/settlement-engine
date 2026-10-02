package com.enterprise.settlement.api;
import com.enterprise.settlement.api.dto.ErrorResponse;
import com.enterprise.settlement.application.usecase.IdempotencyConflictException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
@RestControllerAdvice
public class SettlementExceptionHandler {
    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ErrorResponse> conflict(IdempotencyConflictException ex) { return error(409, ex.getMessage()); }
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> invalid(Exception ex) {
        return error(400, ex instanceof MethodArgumentNotValidException ? "Request validation failed" : ex.getMessage());
    }
    private ResponseEntity<ErrorResponse> error(int status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(message, status, Instant.now()));
    }
}
