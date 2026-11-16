package bankApp;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class MoneyAssert {

    private MoneyAssert() {
    }

    static void assertMoney(String expected, BigDecimal actual) {
        assertTrue(new BigDecimal(expected).compareTo(actual) == 0,
                "Expected " + expected + " but was " + actual);
    }

    static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
