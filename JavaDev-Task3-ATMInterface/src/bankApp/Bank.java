package bankApp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Bank {

    private final String name;
    private final List<Account> accounts = new ArrayList<>();

    public Bank(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int numberOfAccounts() {
        return accounts.size();
    }

    public Account addAccount(String userId, String holderName, String pin, BigDecimal openingBalance) {
        if (findAccount(userId) != null) {
            throw new IllegalArgumentException("An account with this ID already exists");
        }

        Account account = new Account(userId, holderName, pin, openingBalance);
        accounts.add(account);
        return account;
    }

    public Account findAccount(String userId) {
        for (Account account : accounts) {
            if (account.getUserId().equals(userId)) {
                return account;
            }
        }
        return null;
    }

    public Account authenticate(String userId, String pin) {
        Account account = findAccount(userId);
        if (account != null && account.verifyPin(pin)) {
            return account;
        }
        return null;
    }


    public void transfer(Account sender, String recipientId, BigDecimal amount) {
        if (sender == null) {
            throw new IllegalArgumentException("Sender account is required");
        }

        Account recipient = findAccount(recipientId);
        if (recipient == null) {
            throw new IllegalArgumentException("Recipient account not found");
        }
        if (recipient == sender) {
            throw new IllegalArgumentException("You cannot transfer to your own account");
        }

        BigDecimal validAmount = Account.validateAmount(amount);
        if (sender.getBalance().compareTo(validAmount) < 0) {
            throw new InsufficientFundsException();
        }

        sender.transferOut(validAmount, recipient.getUserId());
        recipient.transferIn(validAmount, sender.getUserId());
    }
}
