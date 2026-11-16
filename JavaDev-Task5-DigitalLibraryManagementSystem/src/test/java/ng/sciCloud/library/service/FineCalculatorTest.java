package ng.sciCloud.library.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FineCalculatorTest {

    private final FineCalculator calculator = new FineCalculator(new BigDecimal("5"));
    private final LocalDate due = LocalDate.of(2026, 3, 10);

    @Test
    void returnedBeforeDueDateHasNoFine() {
        assertThat(calculator.calculate(due, due.minusDays(3))).isEqualByComparingTo("0");
    }

    @Test
    void returnedOnDueDateHasNoFine() {
        assertThat(calculator.calculate(due, due)).isEqualByComparingTo("0");
    }

    @Test
    void oneDayLateCostsOneDayOfFine() {
        assertThat(calculator.calculate(due, due.plusDays(1))).isEqualByComparingTo("5");
    }

    @Test
    void tenDaysLateCostsFifty() {
        assertThat(calculator.calculate(due, due.plusDays(10))).isEqualByComparingTo("50");
    }

    @Test
    void fineIsAlwaysReportedWithTwoDecimals() {
        assertThat(calculator.calculate(due, due.plusDays(2)).toPlainString()).isEqualTo("10.00");
        assertThat(calculator.calculate(due, due).toPlainString()).isEqualTo("0.00");
    }

    @Test
    void customRateIsUsed() {
        FineCalculator custom = new FineCalculator(new BigDecimal("7.50"));
        assertThat(custom.calculate(due, due.plusDays(3))).isEqualByComparingTo("22.50");
    }

    @Test
    void rateWithMoreThanTwoDecimalsIsRounded() {
        FineCalculator custom = new FineCalculator(new BigDecimal("0.333"));
        assertThat(custom.calculate(due, due.plusDays(1)).toPlainString()).isEqualTo("0.33");
    }
}
