package bankApp;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static bankApp.MoneyAssert.assertMoney;
import static bankApp.MoneyAssert.money;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransactionTest {

    private final LocalDateTime time = LocalDateTime.of(2026, 3, 5, 14, 7, 9);

    @Test
    public void transactionStoresAllItsDetails() {
        Transaction transaction = new Transaction(
                time, TransactionType.DEPOSIT, money("500.00"), money("1500.00"), "Cash deposit");

        assertEquals(time, transaction.getTimestamp());
        assertEquals(TransactionType.DEPOSIT, transaction.getType());
        assertMoney("500.00", transaction.getAmount());
        assertMoney("1500.00", transaction.getBalanceAfter());
        assertEquals("Cash deposit", transaction.getDetails());
    }

    @Test
    public void timestampIsFormattedForDisplay() {
        Transaction transaction = new Transaction(
                time, TransactionType.DEPOSIT, money("1"), money("1"), "x");

        assertEquals("2026-03-05 14:07:09", transaction.getFormattedTimestamp());
    }

    @Test
    public void transactionCreatedWithoutATimeGetsTheCurrentTime() {
        LocalDateTime before = LocalDateTime.now();
        Transaction transaction = new Transaction(
                TransactionType.DEPOSIT, money("1"), money("1"), "x");
        LocalDateTime after = LocalDateTime.now();

        assertFalse(transaction.getTimestamp().isBefore(before));
        assertFalse(transaction.getTimestamp().isAfter(after));
    }

    @Test
    public void creditTypesAreShownWithAPlusSign() {
        Transaction deposit = new Transaction(time, TransactionType.DEPOSIT, money("500"), money("500"), "x");
        Transaction received = new Transaction(time, TransactionType.TRANSFER_RECEIVED, money("75.5"), money("75.5"), "x");

        assertEquals("+500.00", deposit.getSignedAmount());
        assertEquals("+75.50", received.getSignedAmount());
    }

    @Test
    public void debitTypesAreShownWithAMinusSign() {
        Transaction withdrawal = new Transaction(time, TransactionType.WITHDRAWAL, money("200"), money("0"), "x");
        Transaction sent = new Transaction(time, TransactionType.TRANSFER_SENT, money("1250.75"), money("0"), "x");

        assertEquals("-200.00", withdrawal.getSignedAmount());
        assertEquals("-1,250.75", sent.getSignedAmount());
    }

    @Test
    public void moneyIsFormattedWithSeparatorsAndTwoDecimals() {
        assertEquals("0.00", Transaction.formatMoney(BigDecimal.ZERO));
        assertEquals("12.50", Transaction.formatMoney(money("12.5")));
        assertEquals("1,234,567.89", Transaction.formatMoney(money("1234567.89")));
    }

    @Test
    public void toStringContainsTheKeyInformation() {
        Transaction transaction = new Transaction(
                time, TransactionType.WITHDRAWAL, money("200"), money("800"), "Cash withdrawal");

        String text = transaction.toString();

        assertTrue(text.contains("2026-03-05 14:07:09"));
        assertTrue(text.contains("Withdrawal"));
        assertTrue(text.contains("-200.00"));
        assertTrue(text.contains("800.00"));
        assertTrue(text.contains("Cash withdrawal"));
    }

    @Test
    public void everyTransactionTypeHasALabelAndDirection() {
        assertEquals("Deposit", TransactionType.DEPOSIT.getLabel());
        assertEquals("Withdrawal", TransactionType.WITHDRAWAL.getLabel());
        assertEquals("Transfer Sent", TransactionType.TRANSFER_SENT.getLabel());
        assertEquals("Transfer Received", TransactionType.TRANSFER_RECEIVED.getLabel());

        assertTrue(TransactionType.DEPOSIT.isCredit());
        assertTrue(TransactionType.TRANSFER_RECEIVED.isCredit());
        assertFalse(TransactionType.WITHDRAWAL.isCredit());
        assertFalse(TransactionType.TRANSFER_SENT.isCredit());
    }
}
