package bankApp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static bankApp.MoneyAssert.assertMoney;
import static bankApp.MoneyAssert.money;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BankTest {

    private Bank bank;
    private Account ada;
    private Account tunde;

    @BeforeEach
    public void setUp() {
        bank = new Bank("Test Bank");
        ada = bank.addAccount("1001", "Ada Obi", "1234", money("1000"));
        tunde = bank.addAccount("1002", "Tunde Bello", "4321", money("500"));
    }


    @Test
    public void bankKnowsItsNameAndNumberOfAccounts() {
        assertEquals("Test Bank", bank.getName());
        assertEquals(2, bank.numberOfAccounts());
    }

    @Test
    public void addedAccountCanBeFoundById() {
        assertSame(ada, bank.findAccount("1001"));
        assertSame(tunde, bank.findAccount("1002"));
    }

    @Test
    public void findingAnUnknownAccountReturnsNull() {
        assertNull(bank.findAccount("9999"));
        assertNull(bank.findAccount(null));
    }

    @Test
    public void duplicateUserIdIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> bank.addAccount("1001", "Someone Else", "0000", money("0")));

        assertEquals(2, bank.numberOfAccounts());
    }


    @Test
    public void correctIdAndPinReturnTheAccount() {
        assertSame(ada, bank.authenticate("1001", "1234"));
    }

    @Test
    public void wrongPinIsRejected() {
        assertNull(bank.authenticate("1001", "4321"));
    }

    @Test
    public void unknownUserIsRejected() {
        assertNull(bank.authenticate("9999", "1234"));
    }

    @Test
    public void missingDetailsAreRejected() {
        assertNull(bank.authenticate(null, "1234"));
        assertNull(bank.authenticate("1001", null));
    }

    @Test
    public void onePersonsPinDoesNotOpenAnotherAccount() {
        assertNull(bank.authenticate("1001", "4321"));
        assertNull(bank.authenticate("1002", "1234"));
    }


    @Test
    public void transferMovesMoneyBetweenBothAccounts() {
        bank.transfer(ada, "1002", money("400"));

        assertMoney("600", ada.getBalance());
        assertMoney("900", tunde.getBalance());
    }

    @Test
    public void transferIsLoggedOnBothAccounts() {
        bank.transfer(ada, "1002", money("400"));

        Transaction sent = ada.getTransactions().get(0);
        Transaction received = tunde.getTransactions().get(0);

        assertEquals(TransactionType.TRANSFER_SENT, sent.getType());
        assertEquals("To account 1002", sent.getDetails());
        assertMoney("400", sent.getAmount());
        assertMoney("600", sent.getBalanceAfter());

        assertEquals(TransactionType.TRANSFER_RECEIVED, received.getType());
        assertEquals("From account 1001", received.getDetails());
        assertMoney("400", received.getAmount());
        assertMoney("900", received.getBalanceAfter());
    }

    @Test
    public void canTransferTheEntireBalance() {
        bank.transfer(ada, "1002", money("1000"));

        assertMoney("0", ada.getBalance());
        assertMoney("1500", tunde.getBalance());
    }

    @Test
    public void transferLargerThanBalanceFailsAndChangesNothing() {
        InsufficientFundsException error = assertThrows(InsufficientFundsException.class,
                () -> bank.transfer(ada, "1002", money("1000.01")));

        assertEquals("Insufficient Funds", error.getMessage());
        assertMoney("1000", ada.getBalance());
        assertMoney("500", tunde.getBalance());
        assertTrue(ada.getTransactions().isEmpty());
        assertTrue(tunde.getTransactions().isEmpty());
    }

    @Test
    public void transferToUnknownRecipientFailsAndChangesNothing() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> bank.transfer(ada, "9999", money("100")));

        assertEquals("Recipient account not found", error.getMessage());
        assertMoney("1000", ada.getBalance());
        assertTrue(ada.getTransactions().isEmpty());
    }

    @Test
    public void transferToYourOwnAccountIsRejected() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> bank.transfer(ada, "1001", money("100")));

        assertEquals("You cannot transfer to your own account", error.getMessage());
        assertMoney("1000", ada.getBalance());
        assertTrue(ada.getTransactions().isEmpty());
    }

    @Test
    public void invalidTransferAmountsAreRejectedAndChangeNothing() {
        assertThrows(IllegalArgumentException.class, () -> bank.transfer(ada, "1002", money("0")));
        assertThrows(IllegalArgumentException.class, () -> bank.transfer(ada, "1002", money("-10")));
        assertThrows(IllegalArgumentException.class, () -> bank.transfer(ada, "1002", money("1.234")));
        assertThrows(IllegalArgumentException.class, () -> bank.transfer(ada, "1002", null));

        assertMoney("1000", ada.getBalance());
        assertMoney("500", tunde.getBalance());
    }

    @Test
    public void transferWithoutASenderIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> bank.transfer(null, "1002", money("10")));
    }

    @Test
    public void severalTransfersAddUpCorrectly() {
        bank.transfer(ada, "1002", money("100"));
        bank.transfer(tunde, "1001", money("50"));
        bank.transfer(ada, "1002", money("25.25"));

        assertMoney("924.75", ada.getBalance());
        assertMoney("575.25", tunde.getBalance());
        assertEquals(3, ada.getTransactions().size());
        assertEquals(3, tunde.getTransactions().size());
        assertNotNull(ada.getTransactions().get(1));
    }
}
