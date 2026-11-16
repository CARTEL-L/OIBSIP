package bankApp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Account {

    private static final int MAX_DECIMAL_PLACES = 2;

    private final String userId;
    private final String holderName;
    private final String pin;
    private BigDecimal balance;
    private final List<Transaction> transactions = new ArrayList<>();

    public Account(String userId, String holderName, String pin, BigDecimal openingBalance) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        if (holderName == null || holderName.isBlank()) {
            throw new IllegalArgumentException("Account holder name cannot be empty");
        }
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("PIN must be exactly 4 digits");
        }
        if (openingBalance == null || openingBalance.signum() < 0) {
            throw new IllegalArgumentException("Opening balance cannot be negative");
        }
        if (hasTooManyDecimals(openingBalance)) {
            throw new IllegalArgumentException("Amount cannot have more than 2 decimal places");
        }

        this.userId = userId;
        this.holderName = holderName;
        this.pin = pin;
        this.balance = openingBalance.setScale(MAX_DECIMAL_PLACES, RoundingMode.UNNECESSARY);
    }

    public String getUserId() {
        return userId;
    }

    public String getHolderName() {
        return holderName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public boolean verifyPin(String enteredPin) {
        return enteredPin != null && pin.equals(enteredPin);
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public void deposit(BigDecimal amount) {
        BigDecimal validAmount = validateAmount(amount);

        balance = balance.add(validAmount);
        log(TransactionType.DEPOSIT, validAmount, "Cash deposit");
    }

    public void withdraw(BigDecimal amount) {
        BigDecimal validAmount = validateAmount(amount);
        requireSufficientFunds(validAmount);

        balance = balance.subtract(validAmount);
        log(TransactionType.WITHDRAWAL, validAmount, "Cash withdrawal");
    }

    void transferOut(BigDecimal amount, String recipientId) {
        BigDecimal validAmount = validateAmount(amount);
        requireSufficientFunds(validAmount);

        balance = balance.subtract(validAmount);
        log(TransactionType.TRANSFER_SENT, validAmount, "To account " + recipientId);
    }

    void transferIn(BigDecimal amount, String senderId) {
        BigDecimal validAmount = validateAmount(amount);

        balance = balance.add(validAmount);
        log(TransactionType.TRANSFER_RECEIVED, validAmount, "From account " + senderId);
    }


    static BigDecimal validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (hasTooManyDecimals(amount)) {
            throw new IllegalArgumentException("Amount cannot have more than 2 decimal places");
        }
        return amount.setScale(MAX_DECIMAL_PLACES, RoundingMode.UNNECESSARY);
    }

    private static boolean hasTooManyDecimals(BigDecimal amount) {
        return amount.stripTrailingZeros().scale() > MAX_DECIMAL_PLACES;
    }

    private void requireSufficientFunds(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
    }

    private void log(TransactionType type, BigDecimal amount, String details) {
        transactions.add(new Transaction(type, amount, balance, details));
    }
}
