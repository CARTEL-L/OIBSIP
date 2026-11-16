package bankApp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static bankApp.MoneyAssert.assertMoney;
import static bankApp.MoneyAssert.money;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ATMTest {

    private Bank bank;
    private Account ada;
    private Account tunde;

    @BeforeEach
    public void setUp() {
        bank = new Bank("Test Bank");
        ada = bank.addAccount("1001", "Ada Obi", "1234", money("1000"));
        tunde = bank.addAccount("1002", "Tunde Bello", "4321", money("500"));
    }

    private String run(String... typedLines) {
        String script = String.join("\n", typedLines) + "\n";
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        new ATM(bank, new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)), out).start();

        return buffer.toString(StandardCharsets.UTF_8);
    }


    @Test
    public void startupAsksForUserIdAndPin() {
        String output = run("1001", "1234", "5");

        assertTrue(output.contains("Enter User ID:"));
        assertTrue(output.contains("Enter PIN:"));
    }

    @Test
    public void correctLoginShowsWelcomeAndMenuWithAllFiveOptions() {
        String output = run("1001", "1234", "5");

        assertTrue(output.contains("Login successful. Welcome, Ada Obi!"));
        assertTrue(output.contains("1. Transaction History"));
        assertTrue(output.contains("2. Withdraw"));
        assertTrue(output.contains("3. Deposit"));
        assertTrue(output.contains("4. Transfer"));
        assertTrue(output.contains("5. Quit"));
    }

    @Test
    public void threeWrongAttemptsDenyAccess() {
        String output = run("1001", "0000", "1001", "1111", "1001", "2222");

        assertTrue(output.contains("2 attempt(s) remaining"));
        assertTrue(output.contains("1 attempt(s) remaining"));
        assertTrue(output.contains("Access denied"));
        assertFalse(output.contains("ATM MENU"));
        assertFalse(output.contains("Login successful"));
    }

    @Test
    public void accessStaysDeniedEvenIfCorrectDetailsAreTypedAfterwards() {
        String output = run("1001", "0000", "1001", "1111", "1001", "2222", "1001", "1234", "5");

        assertTrue(output.contains("Access denied"));
        assertFalse(output.contains("Login successful"));
    }

    @Test
    public void unknownUserIdCountsAsAFailedAttempt() {
        String output = run("9999", "1234", "9999", "1234", "9999", "1234");

        assertTrue(output.contains("Access denied"));
    }

    @Test
    public void anotherPersonsPinDoesNotWork() {
        String output = run("1001", "4321", "1001", "4321", "1001", "4321");

        assertTrue(output.contains("Access denied"));
    }

    @Test
    public void loginSucceedsOnTheThirdAttempt() {
        String output = run("1001", "0000", "1001", "1111", "1001", "1234", "5");

        assertTrue(output.contains("Login successful"));
        assertFalse(output.contains("Access denied"));
    }

    @Test
    public void quitShowsGoodbyeMessage() {
        String output = run("1001", "1234", "5");

        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    public void nothingTypedAfterQuitIsProcessed() {
        run("1001", "1234", "5", "3", "999");

        assertMoney("1000", ada.getBalance());
    }

    @Test
    public void withdrawReducesBalanceAndConfirms() {
        String output = run("1001", "1234", "2", "300", "5");

        assertTrue(output.contains("Withdrawal successful"));
        assertTrue(output.contains("New balance: 700.00"));
        assertMoney("700", ada.getBalance());
    }

    @Test
    public void withdrawMoreThanBalanceShowsInsufficientFunds() {
        String output = run("1001", "1234", "2", "5000", "5");

        assertTrue(output.contains("Insufficient Funds"));
        assertTrue(output.contains("Available balance: 1,000.00"));
        assertMoney("1000", ada.getBalance());
        assertTrue(ada.getTransactions().isEmpty());
    }

    @Test
    public void withdrawWithDecimalsWorks() {
        run("1001", "1234", "2", "100.75", "5");

        assertMoney("899.25", ada.getBalance());
    }

    @Test
    public void invalidWithdrawInputIsRejected() {
        String output = run("1001", "1234", "2", "abc", "2", "-5", "2", "10.999", "2", "0", "5");

        assertTrue(output.contains("Invalid amount"));
        assertTrue(output.contains("Amount must be greater than zero"));
        assertMoney("1000", ada.getBalance());
        assertTrue(ada.getTransactions().isEmpty());
    }

    @Test
    public void depositIncreasesBalanceAndConfirms() {
        String output = run("1001", "1234", "3", "250.50", "5");

        assertTrue(output.contains("Deposit successful"));
        assertTrue(output.contains("New balance: 1,250.50"));
        assertMoney("1250.50", ada.getBalance());
    }

    @Test
    public void invalidDepositInputIsRejected() {
        String output = run("1001", "1234", "3", "ten", "3", "0", "5");

        assertTrue(output.contains("Invalid amount"));
        assertTrue(output.contains("Amount must be greater than zero"));
        assertMoney("1000", ada.getBalance());
    }

    @Test
    public void transferUpdatesBothAccounts() {
        String output = run("1001", "1234", "4", "1002", "400", "5");

        assertTrue(output.contains("Transfer successful"));
        assertTrue(output.contains("Tunde Bello"));
        assertMoney("600", ada.getBalance());
        assertMoney("900", tunde.getBalance());
    }

    @Test
    public void transferMoreThanBalanceShowsInsufficientFunds() {
        String output = run("1001", "1234", "4", "1002", "5000", "5");

        assertTrue(output.contains("Insufficient Funds"));
        assertMoney("1000", ada.getBalance());
        assertMoney("500", tunde.getBalance());
    }

    @Test
    public void transferToUnknownAccountIsRejected() {
        String output = run("1001", "1234", "4", "9999", "100", "5");

        assertTrue(output.contains("Recipient account not found"));
        assertMoney("1000", ada.getBalance());
    }

    @Test
    public void transferToOwnAccountIsRejected() {
        String output = run("1001", "1234", "4", "1001", "100", "5");

        assertTrue(output.contains("You cannot transfer to your own account"));
        assertMoney("1000", ada.getBalance());
    }

    @Test
    public void invalidTransferAmountIsRejected() {
        String output = run("1001", "1234", "4", "1002", "lots", "5");

        assertTrue(output.contains("Invalid amount"));
        assertMoney("1000", ada.getBalance());
        assertMoney("500", tunde.getBalance());
    }

    @Test
    public void historyIsEmptyBeforeAnyTransaction() {
        String output = run("1001", "1234", "1", "5");

        assertTrue(output.contains("No transactions yet in this session."));
    }

    @Test
    public void historyListsEveryTransactionInOrder() {
        String output = run("1001", "1234",
                "3", "100",
                "2", "50",
                "4", "1002", "200",
                "1", "5");

        int deposit = output.indexOf("+100.00");
        int withdrawal = output.indexOf("-50.00");
        int transfer = output.indexOf("-200.00");

        assertTrue(output.contains("Transfer Sent"));
        assertTrue(output.contains("Withdrawal"));
        assertTrue(deposit > 0);
        assertTrue(withdrawal > deposit);
        assertTrue(transfer > withdrawal);
    }

    @Test
    public void failedTransactionsAreNotInTheHistory() {
        String output = run("1001", "1234", "2", "99999", "4", "9999", "10", "1", "5");

        assertTrue(output.contains("No transactions yet in this session."));
    }

    @Test
    public void recipientSeesTheReceivedTransfer() {
        bank.transfer(ada, "1002", money("200"));

        String output = run("1002", "4321", "1", "5");

        assertTrue(output.contains("Transfer Received"));
        assertTrue(output.contains("+200.00"));
        assertTrue(output.contains("From account 1001"));
    }

    @Test
    public void historyShowsOnlyTheLoggedInUsersTransactions() {
        ada.deposit(money("123"));

        String output = run("1002", "4321", "1", "5");

        assertTrue(output.contains("No transactions yet in this session."));
        assertFalse(output.contains("123.00"));
    }

    @Test
    public void invalidMenuChoiceShowsMessageAndMenuAgain() {
        String output = run("1001", "1234", "9", "abc", "", "5");

        assertTrue(output.contains("Invalid option. Please enter a number from 1 to 5."));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    public void inputEndingAtLoginEndsTheSessionCleanly() {
        String output = run("1001");

        assertTrue(output.contains("Session ended"));
    }

    @Test
    public void inputEndingInTheMenuEndsTheSessionCleanly() {
        String output = run("1001", "1234");

        assertTrue(output.contains("Session ended"));
    }

    @Test
    public void inputEndingMidTransactionChangesNothing() {
        String output = run("1001", "1234", "2");

        assertTrue(output.contains("Session ended"));
        assertMoney("1000", ada.getBalance());
    }
}
