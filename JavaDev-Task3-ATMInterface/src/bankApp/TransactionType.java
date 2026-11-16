package bankApp;


public enum TransactionType {

    DEPOSIT("Deposit", true),
    WITHDRAWAL("Withdrawal", false),
    TRANSFER_SENT("Transfer Sent", false),
    TRANSFER_RECEIVED("Transfer Received", true);

    private final String label;
    private final boolean credit;

    TransactionType(String label, boolean credit) {
        this.label = label;
        this.credit = credit;
    }

    public String getLabel() {
        return label;
    }

    public boolean isCredit() {
        return credit;
    }
}
