package bankApp;

import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

public class ATM {

    private static final int MAX_LOGIN_ATTEMPTS = 3;

    private final Bank bank;
    private final Scanner scanner;
    private final PrintStream out;
    private Account currentAccount;

    public ATM(Bank bank, InputStream in, PrintStream out) {
        this.bank = bank;
        this.scanner = new Scanner(in, StandardCharsets.UTF_8);
        this.out = out;
    }

    public void start() {
        out.println("=========================================");
        out.println("   Welcome to " + bank.getName() + " ATM");
        out.println("=========================================");

        try {
            if (login()) {
                runMenu();
            }
        } catch (InputClosedException e) {
            out.println();
            out.println("No more input. Session ended.");
        }
    }

    private boolean login() {
        for (int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {
            String userId = readLine("Enter User ID: ");
            String pin = readLine("Enter PIN: ");

            Account account = bank.authenticate(userId, pin);
            if (account != null) {
                currentAccount = account;
                out.println();
                out.println("Login successful. Welcome, " + account.getHolderName() + "!");
                return true;
            }

            int attemptsLeft = MAX_LOGIN_ATTEMPTS - attempt;
            if (attemptsLeft > 0) {
                out.println("Invalid User ID or PIN. " + attemptsLeft + " attempt(s) remaining.");
            }
        }

        out.println("Too many incorrect attempts. Access denied.");
        return false;
    }

    private void runMenu() {
        boolean running = true;

        while (running) {
            printMenu();
            String choice = readLine("Choose an option (1-5): ");

            switch (choice) {
                case "1":
                    showTransactionHistory();
                    break;
                case "2":
                    withdraw();
                    break;
                case "3":
                    deposit();
                    break;
                case "4":
                    transfer();
                    break;
                case "5":
                    out.println();
                    out.println("Thank you for banking with " + bank.getName() + ". Goodbye!");
                    running = false;
                    break;
                default:
                    out.println("Invalid option. Please enter a number from 1 to 5.");
            }
        }
    }

    private void printMenu() {
        out.println();
        out.println("============== ATM MENU ==============");
        out.println("1. Transaction History");
        out.println("2. Withdraw");
        out.println("3. Deposit");
        out.println("4. Transfer");
        out.println("5. Quit");
        out.println("======================================");
    }

    private void showTransactionHistory() {
        List<Transaction> history = currentAccount.getTransactions();

        out.println();
        out.println("====================== TRANSACTION HISTORY ======================");
        if (history.isEmpty()) {
            out.println("No transactions yet in this session.");
            return;
        }

        out.println(String.format("%-4s %-19s %-18s %14s %16s  %s",
                "No.", "Date/Time", "Type", "Amount", "Balance After", "Details"));

        int number = 1;
        for (Transaction transaction : history) {
            out.println(String.format("%-4d %-19s %-18s %14s %16s  %s",
                    number++,
                    transaction.getFormattedTimestamp(),
                    transaction.getType().getLabel(),
                    transaction.getSignedAmount(),
                    Transaction.formatMoney(transaction.getBalanceAfter()),
                    transaction.getDetails()));
        }
        out.println("=================================================================");
    }

    private void withdraw() {
        BigDecimal amount = readAmount("Enter amount to withdraw: ");
        if (amount == null) {
            return;
        }

        try {
            currentAccount.withdraw(amount);
            out.println("Withdrawal successful. Please collect your cash.");
            printBalance();
        } catch (InsufficientFundsException e) {
            out.println(e.getMessage());
            printAvailableBalance();
        } catch (IllegalArgumentException e) {
            out.println(e.getMessage());
        }
    }

    private void deposit() {
        BigDecimal amount = readAmount("Enter amount to deposit: ");
        if (amount == null) {
            return;
        }

        try {
            currentAccount.deposit(amount);
            out.println("Deposit successful.");
            printBalance();
        } catch (IllegalArgumentException e) {
            out.println(e.getMessage());
        }
    }

    private void transfer() {
        String recipientId = readLine("Enter recipient account ID: ");
        BigDecimal amount = readAmount("Enter amount to transfer: ");
        if (amount == null) {
            return;
        }

        try {
            bank.transfer(currentAccount, recipientId, amount);
            Account recipient = bank.findAccount(recipientId);
            out.println("Transfer successful. Sent " + Transaction.formatMoney(amount)
                    + " to " + recipient.getHolderName() + " (account " + recipientId + ").");
            printBalance();
        } catch (InsufficientFundsException e) {
            out.println(e.getMessage());
            printAvailableBalance();
        } catch (IllegalArgumentException e) {
            out.println(e.getMessage());
        }
    }

    private void printBalance() {
        out.println("New balance: " + Transaction.formatMoney(currentAccount.getBalance()));
    }

    private void printAvailableBalance() {
        out.println("Available balance: " + Transaction.formatMoney(currentAccount.getBalance()));
    }

    /** Reads an amount such as 2500 or 2500.50. Prints a message and returns null if it is not valid. */
    private BigDecimal readAmount(String prompt) {
        String text = readLine(prompt);

        if (!text.matches("\\d+(\\.\\d{1,2})?")) {
            out.println("Invalid amount. Enter a positive number, for example 2500 or 2500.50.");
            return null;
        }
        return new BigDecimal(text);
    }

    private String readLine(String prompt) {
        out.print(prompt);
        out.flush();

        if (!scanner.hasNextLine()) {
            throw new InputClosedException();
        }
        return scanner.nextLine().trim();
    }

    private static class InputClosedException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
