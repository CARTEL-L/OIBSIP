# Digital Library Management System

A web-based library management application built for Task 5 of the Java Development internship. The system provides separate Admin and Member experiences for managing books, borrowing, returns, reservations, fines, and contact messages.

## Technology stack

- **Language:** Java 21+
- **Framework:** Spring Boot 3.5.7
- **Web:** Spring MVC and Thymeleaf
- **Security:** Spring Security
- **Persistence:** Spring Data JPA and Hibernate
- **Database:** MySQL 8
- **Frontend:** HTML and custom, responsive CSS
- **Build tool:** Maven
- **Testing:** JUnit 5, Spring Boot Test, Spring Security Test, and H2

The frontend uses a custom design called **The Reading Room**. Its styling is written in regular CSS in `src/main/resources/static/css/style.css`. Tailwind CSS and other utility-CSS frameworks are not used.

## Features

### Member features

| Feature | Page / action |
|---|---|
| Register and log in | `/register`, `/login` |
| Browse the book catalogue | `/books` |
| Search books by title or author and filter by category | Search and category controls on `/books` |
| Borrow an available book | Issue action on `/books` |
| View borrowed books and due dates | `/my/loans` |
| Return a borrowed book | Return action on `/my/loans` |
| Reserve a book when all copies are on loan | Reserve action on `/books` |
| View and cancel reservations | `/my/reservations` |
| Send a query or message to the library | `/contact` |

### Admin features

| Feature | Page / action |
|---|---|
| View the administration dashboard | `/admin` |
| Add, edit, and delete books | `/admin/books` |
| View issued books and due dates | `/admin/loans` |
| View members and their loan history | `/admin/members` |
| Enable or disable a member account | Member management page |
| View outstanding fines and mark them as paid | `/admin/fines` |
| Read contact messages and mark them as resolved | `/admin/messages` |

Admin pages require an authenticated Admin account. Members cannot access Admin-only routes.

## Business rules

The application enforces the following rules:

- The default loan period is **14 days**.
- A member can have at most **5 active loans**.
- The default fine is **5 per day** for a late return.
- A member with an overdue book or an unpaid fine cannot borrow another book until the restriction is cleared.
- A reservation can be placed when all copies of a book are currently issued.
- When copies become available, waiting reservations are handled in queue order.
- A member cannot hold duplicate reservations or borrow duplicate copies of the same book.
- A book with loan history cannot be deleted; its quantity can instead be set to zero.
- The quantity of a book cannot be reduced below the number of copies currently on loan.
- Admin accounts are seeded by the application; public registration creates Member accounts only.

The loan period, maximum active loans, fine amount, currency symbol, seed account details, and sample-book seeding are configurable in `src/main/resources/application.properties`.

## Validation package

Service-level business validation is separated from the service classes and placed in:

```text
src/main/java/ng/sciCloud/library/utils/validators/
```

| Validator | Responsibility |
|---|---|
| `MemberValidation` | Member registration, email availability, member lookup, and account enable/disable checks |
| `BookValidation` | Book lookup, ISBN normalization and uniqueness, book create/update checks, and deletion rules |
| `LoanValidation` | Member/book lookup, borrowing eligibility, return ownership/status, and fine-payment checks |
| `ReservationValidation` | Member/book/reservation lookup, reservation eligibility, and cancellation checks |
| `ContactValidation` | Contact-message lookup |

The service classes delegate these checks to the relevant validator through constructor injection. Services remain responsible for coordinating application operations and persistence.

## Project structure

```text
src/
├── main/
│   ├── java/ng/sciCloud/library/
│   │   ├── config/          # Security, user details, seed data, application properties
│   │   ├── controller/      # Web controllers and global exception handling
│   │   ├── dto/             # Form/request objects
│   │   ├── exception/       # Application-specific exceptions
│   │   ├── model/           # JPA entities and enums
│   │   ├── repository/      # Spring Data JPA repositories
│   │   ├── service/         # Application and business operations
│   │   └── utils/
│   │       └── validators/  # Dedicated service validation classes
│   └── resources/
│       ├── static/css/
│       │   └── style.css    # The Reading Room custom stylesheet
│       ├── templates/       # Thymeleaf pages and shared fragments
│       └── application.properties
└── test/
    ├── java/                # Unit and web-flow tests
    └── resources/           # Test profile configuration

database/
└── schema.sql               # Reference database schema
```

## Requirements

- JDK 21 or newer
- MySQL 8
- Maven 3.6+ (or IntelliJ IDEA with Maven support)

The project uses Java 21 as its Maven compilation target. A newer JDK, such as JDK 27, can also run it.

## Database setup

1. Start your local MySQL server.
2. Open `src/main/resources/application.properties`.
3. Set the MySQL username and password for your machine:

   ```properties
   spring.datasource.username=${DB_USERNAME:root}
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   ```

4. The configured JDBC URL targets a database named `digital_library` and includes `createDatabaseIfNotExist=true`, so MySQL creates that database if it does not already exist and the configured user has permission to do so.
5. Hibernate is configured with `spring.jpa.hibernate.ddl-auto=update` to create/update the application's tables.

Do not commit a real database password to a public repository. Use a local, uncommitted configuration or environment-specific secret management for credentials.

`database/schema.sql` is provided as a reference schema; it is not required for the normal first-run setup described above.

## Run the application

From the project root, run:

```bash
mvn spring-boot:run
```

Alternatively, open the project in IntelliJ IDEA, allow Maven to load the dependencies, and run:

```text
ng.sciCloud.library.DigitalLibraryApplication
```

When Spring Boot has started, open:

<http://localhost:8080>

## Default Admin account

The current seed configuration in `application.properties` uses:

| Field | Value |
|---|---|
| Email | `admin@library.com` |
| Password | `Admin@123` |

The seed configuration also provides the Admin display name and enables sample-book seeding by default. Change the default Admin password before using the application beyond local development.

To disable sample-book seeding, set:

```properties
library.seed-sample-books=false
```

## Configuration

The following settings are available in `src/main/resources/application.properties`:

| Property | Current default | Purpose |
|---|---:|---|
| `library.loan-days` | `14` | Number of days in a standard loan |
| `library.max-active-loans` | `5` | Maximum active loans per member |
| `library.fine-per-day` | `5` | Fine charged for each overdue day |
| `library.currency-symbol` | `₹` | Currency symbol displayed for fines |
| `library.admin-name` | `Library Admin` | Seeded Admin display name |
| `library.admin-email` | `admin@library.com` | Seeded Admin email |
| `library.admin-password` | `Admin@123` | Seeded Admin password |
| `library.seed-sample-books` | `true` | Whether sample books are seeded |

For Naira display, set `library.currency-symbol=₦`.

## Tests

Run the test suite from the project root:

```bash
mvn test
```

The test profile uses an in-memory H2 database, so the MySQL database does not need to be running for tests.

Current test classes include:

- `FineCalculatorTest`
- `BookServiceTest`
- `ContactServiceTest`
- `LoanServiceTest`
- `MemberServiceTest`
- `ReservationServiceTest`
- `LibraryWebFlowTest`

The web-flow tests exercise application routes and security behavior through MockMvc. See the test classes for the exact scenarios covered.

## Main database tables

The application persists data for:

- `users`
- `books`
- `loans`
- `reservations`
- `contact_messages`
