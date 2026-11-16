# ATM Interface

A console-based ATM simulation written in **Java** using an object-oriented design. Users log in with a User ID and PIN, then check their transaction history, withdraw, deposit, transfer money to another account, or quit.

Built as **Task 3 (Java Development)** of the OIBSIP internship programme.

---

## Features

| Requirement | How it works |
|---|---|
| **Login** | The ATM asks for a User ID and PIN. After **3 incorrect attempts** access is denied and the program ends. |
| **Main menu** | After login the menu offers: 1. Transaction History, 2. Withdraw, 3. Deposit, 4. Transfer, 5. Quit. |
| **Transaction History** | Shows every transaction made in the current session: number, date and time, type, signed amount, balance after, and details. |
| **Withdraw** | Asks for an amount, checks the balance, updates it and logs the transaction. |
| **Deposit** | Asks for an amount, updates the balance and logs the transaction. |
| **Transfer** | Asks for the recipient account ID and an amount, checks the balance, updates **both** accounts and logs it on both. |
| **Quit** | Prints a goodbye message and exits. |
| **Insufficient Funds** | The balance is checked before every withdrawal and transfer. If it is too low, the ATM shows `Insufficient Funds` and nothing changes. |
| **Stored in an ArrayList** | Each `Account` keeps its transactions in an `ArrayList<Transaction>`. |

Extra safety checks: amounts must be positive with at most 2 decimal places, you cannot transfer to your own account or to an account that does not exist, and a failed operation never changes any balance or history.

---

## Classes

| Class | Responsibility |
|---|---|
| `Main` | Entry point. Creates the bank, adds the demo accounts and starts the ATM. |
| `ATM` | The console interface: login, menu, prompts and messages. |
| `Account` | One customer's account: ID, name, PIN, balance, and the `ArrayList` of transactions. Deposit and withdraw logic lives here. |
| `Transaction` | An immutable record of one transaction (time, type, amount, balance after, details). |
| `Bank` | Holds all accounts, checks logins, finds accounts and carries out transfers between two accounts. |
| `TransactionType` | Enum: `DEPOSIT`, `WITHDRAWAL`, `TRANSFER_SENT`, `TRANSFER_RECEIVED`. |
| `InsufficientFundsException` | Thrown when a withdrawal or transfer is bigger than the balance. |

Money is stored as `BigDecimal` so amounts like 0.10 + 0.20 add up exactly.

### Project layout

```
src/bankApp/
├── Main.java
├── ATM.java
├── Account.java
├── Transaction.java
├── TransactionType.java
├── Bank.java
└── InsufficientFundsException.java
test/bankApp/
├── ATMTest.java
├── AccountTest.java
├── BankTest.java
├── TransactionTest.java
└── MoneyAssert.java
```

---

## Running the Program

You need **JDK 17 or newer**.

**In IntelliJ IDEA:** open the project folder, then right-click `src/bankApp/Main.java` and choose **Run 'Main.main()'**.

**From the terminal** (in the project folder):

```bash
mkdir -p out
javac -d out src/bankApp/*.java
java -cp out bankApp.Main
```

### Demo accounts

| User ID | PIN | Name | Starting balance |
|---|---|---|---|
| `1001` | `1234` | Ada Obi | 50,000.00 |
| `1002` | `4321` | Tunde Bello | 20,000.00 |
| `1003` | `1111` | Chika Eze | 5,000.00 |

---

## Sample Session

```
=========================================
   Welcome to Demo Bank ATM
=========================================
Enter User ID: 1001
Enter PIN: 1234

Login successful. Welcome, Ada Obi!

============== ATM MENU ==============
1. Transaction History
2. Withdraw
3. Deposit
4. Transfer
5. Quit
======================================
Choose an option (1-5): 2
Enter amount to withdraw: 999999
Insufficient Funds
Available balance: 50,000.00
...
Choose an option (1-5): 1

====================== TRANSACTION HISTORY ======================
No.  Date/Time           Type                       Amount    Balance After  Details
1    2026-11-14 18:24:47 Withdrawal              -1,500.50        48,499.50  Cash withdrawal
2    2026-11-14 18:24:47 Deposit                   +250.00        48,749.50  Cash deposit
3    2026-11-14 18:24:47 Transfer Sent          -10,000.00        38,749.50  To account 1002
=================================================================
```

Three wrong logins:

```
Enter User ID: 1001
Enter PIN: 0000
Invalid User ID or PIN. 2 attempt(s) remaining.
...
Too many incorrect attempts. Access denied.
```

---

## Tests

The project includes JUnit 5 tests for every class. `ATMTest` feeds scripted keyboard input to the ATM and checks the text it prints, so the whole program is covered without typing anything.

| Test class | What it covers |
|---|---|
| `AccountTest` | Account creation rules, PIN check, deposit, withdraw, insufficient funds, transaction log |
| `BankTest` | Adding and finding accounts, login checks, transfers (success, insufficient funds, unknown or own account) |
| `TransactionTest` | Stored details, date format, signed amounts, money formatting |
| `ATMTest` | 3-attempt lockout, every menu option, invalid input, history, and input ending unexpectedly |


---

## Notes and Limitations

- Accounts live in memory only. When the program ends, balances and history reset, which matches the "current session" requirement.
- PINs are stored as plain text, which is fine for a simulation but not for a real bank.
- The PIN is visible as you type, because a plain Java console cannot hide input when run from an IDE.
- Possible extensions: save accounts to a file or database, add a balance enquiry option, add PIN change, hash PINs.

---

## Author

**Princewill Sharon**
OIBSIP Java Development Internship, Task 3
