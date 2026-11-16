package ng.sciCloud.library.service;

import ng.sciCloud.library.IntegrationTestBase;
import ng.sciCloud.library.config.LibraryProperties;
import ng.sciCloud.library.exception.LibraryException;
import ng.sciCloud.library.model.AppUser;
import ng.sciCloud.library.model.Book;
import ng.sciCloud.library.model.FineStatus;
import ng.sciCloud.library.model.Loan;
import ng.sciCloud.library.model.LoanStatus;
import ng.sciCloud.library.model.Reservation;
import ng.sciCloud.library.model.ReservationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoanServiceTest extends IntegrationTestBase {

    @Autowired
    private LoanService loanService;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private LibraryProperties properties;

    @Test
    void issueDecrementsAvailableCopiesAndRecordsDueDate() {
        AppUser member = newMember();
        Book book = newBook(2);

        Loan loan = loanService.issueBook(member.getId(), book.getId());

        assertThat(loan.getId()).isNotNull();
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ISSUED);
        assertThat(loan.getIssueDate()).isEqualTo(LocalDate.now());
        assertThat(loan.getDueDate()).isEqualTo(LocalDate.now().plusDays(properties.getLoanDays()));
        assertThat(loan.getFineStatus()).isEqualTo(FineStatus.NONE);
        assertThat(bookRepository.findById(book.getId()).orElseThrow().getAvailableQuantity()).isEqualTo(1);
        assertThat(bookRepository.findById(book.getId()).orElseThrow().getTotalQuantity()).isEqualTo(2);
    }

    @Test
    void issueIsRefusedWhenNoCopiesRemain() {
        AppUser first = newMember();
        AppUser second = newMember();
        Book book = newBook(1);
        loanService.issueBook(first.getId(), book.getId());

        assertThatThrownBy(() -> loanService.issueBook(second.getId(), book.getId()))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("No copies are available");
        assertThat(bookRepository.findById(book.getId()).orElseThrow().getAvailableQuantity()).isZero();
    }

    @Test
    void sameMemberCannotHoldTwoCopiesOfOneBook() {
        AppUser member = newMember();
        Book book = newBook(3);
        loanService.issueBook(member.getId(), book.getId());

        assertThatThrownBy(() -> loanService.issueBook(member.getId(), book.getId()))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("already have a copy");
    }

    @Test
    void memberCannotExceedMaximumActiveLoans() {
        AppUser member = newMember();
        int max = properties.getMaxActiveLoans();
        for (int i = 0; i < max; i++) {
            loanService.issueBook(member.getId(), newBook(1).getId());
        }
        Book extra = newBook(1);

        assertThatThrownBy(() -> loanService.issueBook(member.getId(), extra.getId()))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("limit of " + max);
    }

    @Test
    void unpaidFineBlocksNewLoans() {
        AppUser member = newMember();
        Loan late = newOverdueLoan(member, newBook(1), 2);
        loanService.returnBook(late.getId(), member.getId());
        Book other = newBook(1);

        assertThatThrownBy(() -> loanService.issueBook(member.getId(), other.getId()))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("unpaid fine");
    }

    @Test
    void paidFineUnblocksNewLoans() {
        AppUser member = newMember();
        Loan late = newOverdueLoan(member, newBook(1), 2);
        loanService.returnBook(late.getId(), member.getId());
        loanService.markFinePaid(late.getId());
        Book other = newBook(1);

        Loan loan = loanService.issueBook(member.getId(), other.getId());

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ISSUED);
    }

    @Test
    void overdueBookBlocksNewLoans() {
        AppUser member = newMember();
        newOverdueLoan(member, newBook(1), 3);
        Book other = newBook(1);

        assertThatThrownBy(() -> loanService.issueBook(member.getId(), other.getId()))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("overdue book");
    }

    @Test
    void issueFailsForUnknownBookOrMember() {
        AppUser member = newMember();
        Book book = newBook(1);

        assertThatThrownBy(() -> loanService.issueBook(member.getId(), -1L))
                .isInstanceOf(LibraryException.class).hasMessageContaining("Book not found");
        assertThatThrownBy(() -> loanService.issueBook(-1L, book.getId()))
                .isInstanceOf(LibraryException.class).hasMessageContaining("Account not found");
    }

    @Test
    void returnOnTimeRestoresCopyAndCreatesNoFine() {
        AppUser member = newMember();
        Book book = newBook(2);
        Loan loan = loanService.issueBook(member.getId(), book.getId());

        Loan returned = loanService.returnBook(loan.getId(), member.getId());

        assertThat(returned.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(returned.getReturnDate()).isEqualTo(LocalDate.now());
        assertThat(returned.getFineStatus()).isEqualTo(FineStatus.NONE);
        assertThat(returned.getFineAmount()).isEqualByComparingTo("0");
        assertThat(bookRepository.findById(book.getId()).orElseThrow().getAvailableQuantity()).isEqualTo(2);
    }

    @Test
    void lateReturnCreatesFineOfRatePerDay() {
        AppUser member = newMember();
        Book book = newBook(1);
        Loan loan = newOverdueLoan(member, book, 4);

        Loan returned = loanService.returnBook(loan.getId(), member.getId());

        BigDecimal expected = properties.getFinePerDay().multiply(BigDecimal.valueOf(4));
        assertThat(returned.getFineAmount()).isEqualByComparingTo(expected);
        assertThat(returned.getFineAmount()).isEqualByComparingTo("20");
        assertThat(returned.getFineStatus()).isEqualTo(FineStatus.UNPAID);
        assertThat(bookRepository.findById(book.getId()).orElseThrow().getAvailableQuantity()).isEqualTo(1);
    }

    @Test
    void returnOnDueDateIsNotLate() {
        AppUser member = newMember();
        Book book = newBook(1);
        Loan loan = newOverdueLoan(member, book, 0);

        Loan returned = loanService.returnBook(loan.getId(), member.getId());

        assertThat(returned.getFineStatus()).isEqualTo(FineStatus.NONE);
    }

    @Test
    void cannotReturnTheSameLoanTwice() {
        AppUser member = newMember();
        Loan loan = loanService.issueBook(member.getId(), newBook(1).getId());
        loanService.returnBook(loan.getId(), member.getId());

        assertThatThrownBy(() -> loanService.returnBook(loan.getId(), member.getId()))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("already been returned");
    }

    @Test
    void cannotReturnSomeoneElsesLoan() {
        AppUser owner = newMember();
        AppUser other = newMember();
        Loan loan = loanService.issueBook(owner.getId(), newBook(1).getId());

        assertThatThrownBy(() -> loanService.returnBook(loan.getId(), other.getId()))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("does not belong to you");
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ISSUED);
    }

    @Test
    void returnFailsForUnknownLoan() {
        AppUser member = newMember();

        assertThatThrownBy(() -> loanService.returnBook(-1L, member.getId()))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("Loan not found");
    }

    @Test
    void markFinePaidSetsStatusAndTimestamp() {
        AppUser member = newMember();
        Loan loan = newOverdueLoan(member, newBook(1), 2);
        loanService.returnBook(loan.getId(), member.getId());

        Loan paid = loanService.markFinePaid(loan.getId());

        assertThat(paid.getFineStatus()).isEqualTo(FineStatus.PAID);
        assertThat(paid.getFinePaidAt()).isNotNull();
    }

    @Test
    void markFinePaidRejectsLoansWithoutAnUnpaidFine() {
        AppUser member = newMember();
        Loan clean = loanService.issueBook(member.getId(), newBook(1).getId());
        loanService.returnBook(clean.getId(), member.getId());
        Loan late = newOverdueLoan(member, newBook(1), 1);
        loanService.returnBook(late.getId(), member.getId());
        loanService.markFinePaid(late.getId());

        assertThatThrownBy(() -> loanService.markFinePaid(clean.getId()))
                .isInstanceOf(LibraryException.class).hasMessageContaining("no unpaid fine");
        assertThatThrownBy(() -> loanService.markFinePaid(late.getId()))
                .isInstanceOf(LibraryException.class).hasMessageContaining("no unpaid fine");
        assertThatThrownBy(() -> loanService.markFinePaid(-1L))
                .isInstanceOf(LibraryException.class).hasMessageContaining("Loan not found");
    }

    @Test
    void fineTotalsSplitByStatus() {
        BigDecimal unpaidBefore = loanService.totalFines(FineStatus.UNPAID);
        BigDecimal paidBefore = loanService.totalFines(FineStatus.PAID);
        AppUser member = newMember();
        Loan one = newOverdueLoan(member, newBook(1), 2);
        Loan two = newOverdueLoan(member, newBook(1), 3);
        loanService.returnBook(one.getId(), member.getId());
        loanService.returnBook(two.getId(), member.getId());
        loanService.markFinePaid(two.getId());

        assertThat(loanService.totalFines(FineStatus.UNPAID)).isEqualByComparingTo(unpaidBefore.add(new BigDecimal("10")));
        assertThat(loanService.totalFines(FineStatus.PAID)).isEqualByComparingTo(paidBefore.add(new BigDecimal("15")));
    }

    @Test
    void listingsReturnTheRightLoans() {
        AppUser member = newMember();
        AppUser other = newMember();
        Book bookA = newBook(1);
        Book bookB = newBook(1);
        Loan active = loanService.issueBook(member.getId(), bookA.getId());
        Loan late = newOverdueLoan(member, bookB, 2);
        loanService.returnBook(late.getId(), member.getId());
        Loan others = loanService.issueBook(other.getId(), newBook(1).getId());

        assertThat(loanService.loansForUser(member.getId())).extracting(Loan::getId)
                .containsExactlyInAnyOrder(active.getId(), late.getId());
        assertThat(loanService.issuedLoans()).extracting(Loan::getId)
                .contains(active.getId(), others.getId()).doesNotContain(late.getId());
        assertThat(loanService.loansWithFines()).extracting(Loan::getId).contains(late.getId());
        assertThat(loanService.loansWithFines()).extracting(Loan::getId).doesNotContain(active.getId());
        assertThat(loanService.issuedBookIdsFor(member.getId())).containsExactly(bookA.getId());
    }

    @Test
    void isOverdueOnlyAppliesToIssuedLoansPastDueDate() {
        AppUser member = newMember();
        Loan fresh = loanService.issueBook(member.getId(), newBook(1).getId());
        Loan late = newOverdueLoan(member, newBook(1), 1);

        assertThat(fresh.isOverdue(LocalDate.now())).isFalse();
        assertThat(fresh.isOverdue(fresh.getDueDate())).isFalse();
        assertThat(fresh.isOverdue(fresh.getDueDate().plusDays(1))).isTrue();
        assertThat(late.isOverdue(LocalDate.now())).isTrue();
        loanService.returnBook(late.getId(), member.getId());
        assertThat(late.isOverdue(LocalDate.now())).isFalse();
    }

    @Test
    void freeCopiesAreHeldForEarlierReservations() {
        AppUser holder = newMember();
        AppUser first = newMember();
        AppUser second = newMember();
        Book book = newBook(1);
        Loan loan = loanService.issueBook(holder.getId(), book.getId());
        Reservation firstReservation = reservationService.reserve(first.getId(), book.getId());
        Reservation secondReservation = reservationService.reserve(second.getId(), book.getId());
        loanService.returnBook(loan.getId(), holder.getId());

        assertThatThrownBy(() -> loanService.issueBook(second.getId(), book.getId()))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("held for members who reserved");
        assertThatThrownBy(() -> loanService.issueBook(holder.getId(), book.getId()))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("held for members who reserved");

        loanService.issueBook(first.getId(), book.getId());

        assertThat(firstReservation.getStatus()).isEqualTo(ReservationStatus.FULFILLED);
        assertThat(secondReservation.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
    }

    @Test
    void cancelledReservationNoLongerHoldsACopy() {
        AppUser holder = newMember();
        AppUser waiting = newMember();
        AppUser walkIn = newMember();
        Book book = newBook(1);
        Loan loan = loanService.issueBook(holder.getId(), book.getId());
        Reservation reservation = reservationService.reserve(waiting.getId(), book.getId());
        loanService.returnBook(loan.getId(), holder.getId());
        reservationService.cancel(reservation.getId(), waiting.getId());

        Loan issued = loanService.issueBook(walkIn.getId(), book.getId());

        assertThat(issued.getStatus()).isEqualTo(LoanStatus.ISSUED);
    }

    @Test
    void extraCopiesBeyondTheReservationQueueAreOpenToEveryone() {
        AppUser holderA = newMember();
        AppUser holderB = newMember();
        AppUser waiting = newMember();
        AppUser walkIn = newMember();
        Book book = newBook(2);
        Loan loanA = loanService.issueBook(holderA.getId(), book.getId());
        Loan loanB = loanService.issueBook(holderB.getId(), book.getId());
        reservationService.reserve(waiting.getId(), book.getId());
        loanService.returnBook(loanA.getId(), holderA.getId());
        loanService.returnBook(loanB.getId(), holderB.getId());

        Loan issued = loanService.issueBook(walkIn.getId(), book.getId());

        assertThat(issued.getStatus()).isEqualTo(LoanStatus.ISSUED);
    }
}
