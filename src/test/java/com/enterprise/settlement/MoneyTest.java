package com.enterprise.settlement;
import com.enterprise.settlement.domain.model.Money;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;
class MoneyTest {
 @Test void rejectsInvalidCurrencyAndPrecision() {
  assertThatThrownBy(() -> new Money(new BigDecimal("1"), "ZZZ")).isInstanceOf(IllegalArgumentException.class);
  assertThatThrownBy(() -> new Money(new BigDecimal("1.00001"), "EUR")).isInstanceOf(IllegalArgumentException.class);
  assertThatThrownBy(() -> new Money(new BigDecimal("1000000000000000"), "EUR")).isInstanceOf(IllegalArgumentException.class);
  assertThatThrownBy(() -> new Money(BigDecimal.ZERO, "EUR")).isInstanceOf(IllegalArgumentException.class);
  assertThat(new Money(new BigDecimal("1.20000"), "EUR").currency()).isEqualTo("EUR");
 }
}
