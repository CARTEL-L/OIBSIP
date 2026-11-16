package bankApp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static bankApp.MoneyAssert.assertMoney;
import static bankApp.MoneyAssert.money;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountTest {

    private Account account;

    @BeforeEach
    public void setUp() {
        account = new Account("1001", "Ada Obi", "1234", money("1000"));
    }


    @Test
    public void newAccountStoresItsDetailsAndOpeningBalance() {
        assertEquals("1001", account.getUserId());
        assertEquals("Ada Obi", account.getHolderName());
        assertMoney("1000.00", account.getBalance());
    }

    @Test
    public void newAccountHasNoTransactions() {
        assertTrue(account.getTransactions().isEmpty());
    }

    @Test
    public void accountCanBeOpenedWithZeroBalance() {
        Account empty = new Account("1002", "Tunde Bello", "4321", BigDecimal.ZERO);

        assertMoney("0", empty.getBalance());
    }

    @Test
    public void blankUserIdIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Account(" ", "Ada", "1234", money("0")));
        assertThrows(IllegalArgumentException.class, () -> new Account(null, "Ada", "1234", money("0")));
    }

    @Test
    public void blankHolderNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Account("1", "", "1234", money("0")));
        assertThrows(IllegalArgumentException.class, () -> new Account("1", null, "1234", money("0")));
    }

    @Test
    public void pinMustBeExactlyFourDigits() {
        assertThrows(IllegalArgumentException.class, () -> new Account("1", "Ada", "123", money("0")));
        assertThrows(IllegalArgumentException.class, () -> new Account("1", "Ada", "12345", money("0")));
        assertThrows(IllegalArgumentException.class, () -> new Account("1", "Ada", "12a4", money("0")));
        assertThrows(IllegalArgumentException.class, () -> new Account("1", "Ada", null, money("0")));
    }

    @Test
    public void openingBalanceCannotBeNegativeOrMissing() {
        assertThrows(IllegalArgumentException.class, () -> new Account("1", "Ada", "1234", money("-1")));
        assertThrows(IllegalArgumentException.class, () -> new Account("1", "Ada", "1234", null));
    }

    @Test
    public void openingBalanceCannotHaveMoreThanTwoDecimals() {
        assertThrows(IllegalArgumentException.class, () -> new Account("1", "Ada", "1234", money("10.005")));
    }

    @Test
    public void correctPinIsAccepted() {
        assertTrue(account.verifyPin("1234"));
    }

    @Test
    public void wrongOrMissingPinIsRejected() {
        assertFalse(account.verifyPin("4321"));
        assertFalse(account.verifyPin(""));
        assertFalse(account.verifyPin(null));
    }

    @Test
    public void depositIncreasesTheBalance() {
        account.deposit(money("250.50"));

        assertMoney("1250.50", account.getBalance());
    }

    @Test
    public void depositsAddUp() {
        account.deposit(money("100"));
        account.deposit(money("0.10"));
        account.deposit(money("0.20"));

        assertMoney("1100.30", account.getBalance());
    }

    @Test
    public void invalidDepositAmountsAreRejectedAndChangeNothing() {
        assertThrows(IllegalArgumentException.class, () -> account.deposit(money("0")));
        assertThrows(IllegalArgumentException.class, () -> account.deposit(money("-50")));
        assertThrows(IllegalArgumentException.class, () -> account.deposit(money("1.005")));
        assertThrows(IllegalArgumentException.class, () -> account.deposit(null));

        assertMoney("1000", account.getBalance());
        assertTrue(account.getTransactions().isEmpty());
    }

    @Test
    public void withdrawReducesTheBalance() {
        account.withdraw(money("300"));

        assertMoney("700", account.getBalance());
    }

    @Test
    public void canWithdrawTheEntireBalance() {
        account.withdraw(money("1000"));

        assertMoney("0", account.getBalance());
    }

    @Test
    public void withdrawingMoreThanTheBalanceFailsWithInsufficientFunds() {
        InsufficientFundsException error = assertThrows(InsufficientFundsException.class,
                () -> account.withdraw(money("1000.01")));

        assertEquals("Insufficient Funds", error.getMessage());
    }

    @Test
    public void failedWithdrawalLeavesBalanceAndHistoryUntouched() {
        assertThrows(InsufficientFundsException.class, () -> account.withdraw(money("5000")));

        assertMoney("1000", account.getBalance());
        assertTrue(account.getTransactions().isEmpty());
    }

    @Test
    public void invalidWithdrawalAmountsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(money("0")));
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(money("-5")));
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(money("2.555")));
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(null));

        assertMoney("1000", account.getBalance());
    }

    @Test
    public void transferOutReducesBalanceAndLogsTheRecipient() {
        account.transferOut(money("400"), "1002");

        assertMoney("600", account.getBalance());
        Transaction logged = account.getTransactions().get(0);
        assertEquals(TransactionType.TRANSFER_SENT, logged.getType());
        assertEquals("To account 1002", logged.getDetails());
    }

    @Test
    public void transferOutChecksTheBalance() {
        assertThrows(InsufficientFundsException.class, () -> account.transferOut(money("1001"), "1002"));

        assertMoney("1000", account.getBalance());
        assertTrue(account.getTransactions().isEmpty());
    }

    @Test
    public void transferInIncreasesBalanceAndLogsTheSender() {
        account.transferIn(money("250"), "1002");

        assertMoney("1250", account.getBalance());
        Transaction logged = account.getTransactions().get(0);
        assertEquals(TransactionType.TRANSFER_RECEIVED, logged.getType());
        assertEquals("From account 1002", logged.getDetails());
    }

    @Test
    public void everySuccessfulOperationIsLoggedInOrder() {
        account.deposit(money("500"));
        account.withdraw(money("200"));
        account.transferOut(money("100"), "1002");
        account.transferIn(money("50"), "1003");

        List<Transaction> history = account.getTransactions();

        assertEquals(4, history.size());
        assertEquals(TransactionType.DEPOSIT, history.get(0).getType());
        assertEquals(TransactionType.WITHDRAWAL, history.get(1).getType());
        assertEquals(TransactionType.TRANSFER_SENT, history.get(2).getType());
        assertEquals(TransactionType.TRANSFER_RECEIVED, history.get(3).getType());
    }

    @Test
    public void eachLogEntryRecordsAmountAndBalanceAfter() {
        account.deposit(money("500"));
        account.withdraw(money("200"));

        Transaction deposit = account.getTransactions().get(0);
        Transaction withdrawal = account.getTransactions().get(1);

        assertMoney("500", deposit.getAmount());
        assertMoney("1500", deposit.getBalanceAfter());
        assertMoney("200", withdrawal.getAmount());
        assertMoney("1300", withdrawal.getBalanceAfter());
    }

    @Test
    public void transactionListCannotBeChangedFromOutside() {
        account.deposit(money("10"));
        List<Transaction> history = account.getTransactions();
        Transaction extra = new Transaction(TransactionType.DEPOSIT, money("1"), money("1"), "fake");

        assertThrows(UnsupportedOperationException.class, () -> history.add(extra));
        assertThrows(UnsupportedOperationException.class, () -> history.remove(0));
    }
}
