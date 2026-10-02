package com.enterprise.settlement.domain.model;
import java.math.BigDecimal;
import java.util.Currency;
public record Money(BigDecimal amount, String currency) {
    public Money {
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (amount.stripTrailingZeros().scale() > 4 || amount.compareTo(new BigDecimal("1000000000000000")) >= 0)
            throw new IllegalArgumentException("Amount exceeds supported decimal(19,4) precision");
        if (currency == null || !currency.matches("[A-Z]{3}")) throw new IllegalArgumentException("Use an uppercase ISO currency code");
        Currency.getInstance(currency);
    }
}
