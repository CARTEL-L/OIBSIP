package ng.sciCloud.library.service;

import ng.sciCloud.library.IntegrationTestBase;
import ng.sciCloud.library.exception.LibraryException;
import ng.sciCloud.library.model.AppUser;
import ng.sciCloud.library.model.Book;
import ng.sciCloud.library.model.Reservation;
import ng.sciCloud.library.model.ReservationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationServiceTest extends IntegrationTestBase {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private LoanService loanService;

    @Test
    void reserveWorksWhenEveryCopyIsIssuedToSomeoneElse() {
        AppUser holder = newMember();
        AppUser waiting = newMember();
        Book book = newBook(1);
        loanService.issueBook(holder.getId(), book.getId());

        Reservation reservation = reservationService.reserve(waiting.getId(), book.getId());

        assertThat(reservation.getId()).isNotNull();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
        assertThat(reservation.getReservedAt()).isNotNull();
        assertThat(reservationService.reservedBookIdsFor(waiting.getId())).containsExactly(book.getId());
    }

    @Test
    void reserveIsRefusedWhileACopyIsAvailable() {
        AppUser member = newMember();
        Book book = newBook(2);

        assertThatThrownBy(() -> reservationService.reserve(member.getId(), book.getId()))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("available right now");
    }

    @Test
    void holderOfTheBookCannotReserveIt() {
        AppUser holder = newMember();
        Book book = newBook(1);
        loanService.issueBook(holder.getId(), book.getId());

        assertThatThrownBy(() -> reservationService.reserve(holder.getId(), book.getId()))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("already have this book");
    }

    @Test
    void sameBookCannotBeReservedTwiceByOneMember() {
        AppUser holder = newMember();
        AppUser waiting = newMember();
        Book book = newBook(1);
        loanService.issueBook(holder.getId(), book.getId());
        reservationService.reserve(waiting.getId(), book.getId());

        assertThatThrownBy(() -> reservationService.reserve(waiting.getId(), book.getId()))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("already reserved");
    }

    @Test
    void reserveFailsForUnknownBookOrMember() {
        AppUser member = newMember();
        Book book = newBook(0);

        assertThatThrownBy(() -> reservationService.reserve(member.getId(), -1L))
                .isInstanceOf(LibraryException.class).hasMessageContaining("Book not found");
        assertThatThrownBy(() -> reservationService.reserve(-1L, book.getId()))
                .isInstanceOf(LibraryException.class).hasMessageContaining("Account not found");
    }

    @Test
    void zeroQuantityBookCanBeReserved() {
        AppUser member = newMember();
        Book book = newBook(0);

        Reservation reservation = reservationService.reserve(member.getId(), book.getId());

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
    }

    @Test
    void memberCanCancelOwnReservationAndReserveAgain() {
        AppUser holder = newMember();
        AppUser waiting = newMember();
        Book book = newBook(1);
        loanService.issueBook(holder.getId(), book.getId());
        Reservation reservation = reservationService.reserve(waiting.getId(), book.getId());

        reservationService.cancel(reservation.getId(), waiting.getId());

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(reservationService.reservedBookIdsFor(waiting.getId())).isEmpty();
        Reservation again = reservationService.reserve(waiting.getId(), book.getId());
        assertThat(again.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
    }

    @Test
    void cancelRulesAreEnforced() {
        AppUser holder = newMember();
        AppUser waiting = newMember();
        AppUser stranger = newMember();
        Book book = newBook(1);
        loanService.issueBook(holder.getId(), book.getId());
        Reservation reservation = reservationService.reserve(waiting.getId(), book.getId());

        assertThatThrownBy(() -> reservationService.cancel(reservation.getId(), stranger.getId()))
                .isInstanceOf(LibraryException.class).hasMessageContaining("does not belong to you");
        assertThatThrownBy(() -> reservationService.cancel(-1L, waiting.getId()))
                .isInstanceOf(LibraryException.class).hasMessageContaining("Reservation not found");

        reservationService.cancel(reservation.getId(), waiting.getId());
        assertThatThrownBy(() -> reservationService.cancel(reservation.getId(), waiting.getId()))
                .isInstanceOf(LibraryException.class).hasMessageContaining("Only active reservations");
    }

    @Test
    void reservationsForUserListsNewestFirst() {
        AppUser holder = newMember();
        AppUser waiting = newMember();
        Book first = newBook(1);
        Book second = newBook(1);
        loanService.issueBook(holder.getId(), first.getId());
        loanService.issueBook(holder.getId(), second.getId());
        Reservation older = reservationService.reserve(waiting.getId(), first.getId());
        older.setReservedAt(older.getReservedAt().minusHours(1));
        Reservation newer = reservationService.reserve(waiting.getId(), second.getId());

        assertThat(reservationService.reservationsForUser(waiting.getId())).extracting(Reservation::getId)
                .containsExactly(newer.getId(), older.getId());
    }
}
