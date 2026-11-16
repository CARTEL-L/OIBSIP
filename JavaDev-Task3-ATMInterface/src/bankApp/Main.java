package bankApp;

import java.math.BigDecimal;

public class Main {

    public static void main(String[] args) {
        Bank bank = new Bank("Demo Bank");

        bank.addAccount("1001", "Ada Obi", "1234", new BigDecimal("50000.00"));
        bank.addAccount("1002", "Tunde Bello", "4321", new BigDecimal("20000.00"));
        bank.addAccount("1003", "Chika Eze", "1111", new BigDecimal("5000.00"));

        ATM atm = new ATM(bank, System.in, System.out);
        atm.start();
    }
}
