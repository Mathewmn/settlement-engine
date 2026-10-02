package com.enterprise.settlement.api;

import com.enterprise.settlement.application.dto.ProcessSettlementCommand;
import com.enterprise.settlement.application.dto.SettlementResponse;
import com.enterprise.settlement.application.usecase.ProcessSettlementUseCase;
import com.enterprise.settlement.api.dto.CreateSettlementRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settlements")
public class SettlementController {

    private final ProcessSettlementUseCase processSettlementUseCase;

    public SettlementController(ProcessSettlementUseCase processSettlementUseCase) {
        this.processSettlementUseCase = processSettlementUseCase;
    }

    @PostMapping
    public ResponseEntity<SettlementResponse> createSettlement(
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @Valid @RequestBody CreateSettlementRequest request
    ) {
        ProcessSettlementCommand cmd = new ProcessSettlementCommand(
            idempotencyKey,
            request.debtorIban(),
            request.creditorIban(),
            request.amount(),
            request.currency()
        );

        SettlementResponse response = processSettlementUseCase.execute(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
