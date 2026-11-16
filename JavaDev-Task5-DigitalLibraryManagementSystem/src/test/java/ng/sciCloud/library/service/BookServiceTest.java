package ng.sciCloud.library.service;

import ng.sciCloud.library.IntegrationTestBase;
import ng.sciCloud.library.dto.BookForm;
import ng.sciCloud.library.exception.LibraryException;
import ng.sciCloud.library.model.AppUser;
import ng.sciCloud.library.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookServiceTest extends IntegrationTestBase {

    @Autowired
    private BookService bookService;
    @Autowired
    private LoanService loanService;
    @Autowired
    private ReservationService reservationService;

    private BookForm form(String title, String author, String isbn, String category, int quantity) {
        BookForm form = new BookForm();
        form.setTitle(title);
        form.setAuthor(author);
        form.setIsbn(isbn);
        form.setCategory(category);
        form.setQuantity(quantity);
        return form;
    }

    @Test
    void createStoresAllFieldsAndStartsFullyAvailable() {
        Book book = bookService.create(form("  Dune  ", "Frank Herbert", "978-0-441-17271-9", "Sci-Fi", 4));

        assertThat(book.getId()).isNotNull();
        assertThat(book.getTitle()).isEqualTo("Dune");
        assertThat(book.getAuthor()).isEqualTo("Frank Herbert");
        assertThat(book.getIsbn()).isEqualTo("9780441172719");
        assertThat(book.getCategory()).isEqualTo("Sci-Fi");
        assertThat(book.getTotalQuantity()).isEqualTo(4);
        assertThat(book.getAvailableQuantity()).isEqualTo(4);
    }

    @Test
    void createRejectsDuplicateIsbnEvenWithDifferentFormatting() {
        bookService.create(form("Dune", "Frank Herbert", "9780441172719", "Sci-Fi", 1));

        assertThatThrownBy(() -> bookService.create(form("Dune 2", "Someone", "978-0-441-17271-9", "Sci-Fi", 1)))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createRejectsIsbnOfWrongLength() {
        assertThatThrownBy(() -> bookService.create(form("Short", "Author", "12345678901", "Misc", 1)))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("10 or 13");
    }

    @Test
    void updateChangesFieldsAndRecalculatesAvailableCopies() {
        AppUser member = newMember();
        Book book = bookService.create(form("Old title", "Old Author", "9780441172719", "Old", 3));
        loanService.issueBook(member.getId(), book.getId());

        Book updated = bookService.update(book.getId(), form("New title", "New Author", "9780441172719", "New", 5));

        assertThat(updated.getTitle()).isEqualTo("New title");
        assertThat(updated.getAuthor()).isEqualTo("New Author");
        assertThat(updated.getCategory()).isEqualTo("New");
        assertThat(updated.getTotalQuantity()).isEqualTo(5);
        assertThat(updated.getAvailableQuantity()).isEqualTo(4);
    }

    @Test
    void updateCannotReduceQuantityBelowIssuedCopies() {
        AppUser a = newMember();
        AppUser b = newMember();
        Book book = bookService.create(form("Popular", "Author", "9780441172719", "Misc", 2));
        loanService.issueBook(a.getId(), book.getId());
        loanService.issueBook(b.getId(), book.getId());

        assertThatThrownBy(() -> bookService.update(book.getId(), form("Popular", "Author", "9780441172719", "Misc", 1)))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("currently issued");
    }

    @Test
    void updateCannotTakeAnotherBooksIsbn() {
        bookService.create(form("First", "Author", "9780441172719", "Misc", 1));
        Book second = bookService.create(form("Second", "Author", "9780132350884", "Misc", 1));

        assertThatThrownBy(() -> bookService.update(second.getId(), form("Second", "Author", "9780441172719", "Misc", 1)))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("already uses ISBN");
    }

    @Test
    void updateKeepingOwnIsbnIsAllowed() {
        Book book = bookService.create(form("First", "Author", "9780441172719", "Misc", 1));

        Book updated = bookService.update(book.getId(), form("Renamed", "Author", "978-0-441-17271-9", "Misc", 1));

        assertThat(updated.getTitle()).isEqualTo("Renamed");
    }

    @Test
    void deleteRemovesBookWithoutHistory() {
        Book book = bookService.create(form("Temp", "Author", "9780441172719", "Misc", 1));

        bookService.delete(book.getId());

        assertThat(bookRepository.findById(book.getId())).isEmpty();
    }

    @Test
    void deleteAlsoRemovesItsReservations() {
        AppUser holder = newMember();
        AppUser waiter = newMember();
        Book book = bookService.create(form("Temp", "Author", "9780441172719", "Misc", 1));
        loanService.issueBook(holder.getId(), book.getId());
        reservationService.reserve(waiter.getId(), book.getId());
        loanRepository.deleteAll();
        bookRepository.flush();

        bookService.delete(book.getId());

        assertThat(bookRepository.findById(book.getId())).isEmpty();
        assertThat(reservationRepository.findByUserIdOrderByReservedAtDesc(waiter.getId())).isEmpty();
    }

    @Test
    void deleteIsRefusedWhenBookHasLoanHistory() {
        AppUser member = newMember();
        Book book = bookService.create(form("Borrowed", "Author", "9780441172719", "Misc", 1));
        loanService.issueBook(member.getId(), book.getId());

        assertThatThrownBy(() -> bookService.delete(book.getId()))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("loan history");
        assertThat(bookRepository.findById(book.getId())).isPresent();
    }

    @Test
    void getThrowsForUnknownId() {
        assertThatThrownBy(() -> bookService.get(-1L))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void searchMatchesTitleOrAuthorIgnoringCase() {
        bookService.create(form("Zebra Crossing", "Quentin Quokka", "9780441172719", "Nature", 1));
        bookService.create(form("Other Title", "Zelda Zebra", "9780132350884", "Games", 1));
        bookService.create(form("Unrelated", "Someone Else", "9780134685991", "Misc", 1));

        assertThat(bookService.search("zebra", "")).extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Zebra Crossing", "Other Title");
        assertThat(bookService.search("QUOKKA", null)).extracting(Book::getTitle)
                .containsExactly("Zebra Crossing");
    }

    @Test
    void searchFiltersByCategoryAndCombinesWithQuery() {
        bookService.create(form("Zebra Crossing", "Quentin Quokka", "9780441172719", "Nature", 1));
        bookService.create(form("Zebra Games", "Someone", "9780132350884", "Games", 1));

        assertThat(bookService.search("", "Games")).extracting(Book::getTitle).containsExactly("Zebra Games");
        assertThat(bookService.search("zebra", "Nature")).extracting(Book::getTitle).containsExactly("Zebra Crossing");
        assertThat(bookService.search("nothing-matches", "Nature")).isEmpty();
    }

    @Test
    void categoriesAreDistinctAndSorted() {
        bookService.create(form("A", "Author", "9780441172719", "Beta", 1));
        bookService.create(form("B", "Author", "9780132350884", "Alpha", 1));
        bookService.create(form("C", "Author", "9780134685991", "Beta", 1));

        assertThat(bookService.categories()).containsExactly("Alpha", "Beta");
    }
}
