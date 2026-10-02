package com.enterprise.settlement.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateSettlementRequest(
    @NotBlank(message = "Debtor IBAN is required")
    @Size(max = 34)
    String debtorIban,

    @NotBlank(message = "Creditor IBAN is required")
    @Size(max = 34)
    String creditorIban,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 15, fraction = 4)
    BigDecimal amount,

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must contain three uppercase letters")
    String currency
) {}
