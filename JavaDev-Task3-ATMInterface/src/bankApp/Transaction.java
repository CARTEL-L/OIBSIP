package bankApp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Transaction {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LocalDateTime timestamp;
    private final TransactionType type;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final String details;

    public Transaction(TransactionType type, BigDecimal amount,
                       BigDecimal balanceAfter, String details) {
        this(LocalDateTime.now(), type, amount, balanceAfter, details);
    }

    public Transaction(LocalDateTime timestamp, TransactionType type, BigDecimal amount,
                       BigDecimal balanceAfter, String details) {
        this.timestamp = timestamp;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getDetails() {
        return details;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(TIME_FORMAT);
    }

    public String getSignedAmount() {
        return (type.isCredit() ? "+" : "-") + formatMoney(amount);
    }

    public static String formatMoney(BigDecimal value) {
        return String.format(Locale.US, "%,.2f", value);
    }

    @Override
    public String toString() {
        return getFormattedTimestamp() + " | " + type.getLabel() + " | " + getSignedAmount()
                + " | Balance: " + formatMoney(balanceAfter) + " | " + details;
    }
}
