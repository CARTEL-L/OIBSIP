package ng.sciCloud.library.web;

import ng.sciCloud.library.IntegrationTestBase;
import ng.sciCloud.library.model.AppUser;
import ng.sciCloud.library.model.Book;
import ng.sciCloud.library.model.FineStatus;
import ng.sciCloud.library.model.Loan;
import ng.sciCloud.library.model.LoanStatus;
import ng.sciCloud.library.model.ReservationStatus;
import ng.sciCloud.library.repository.ContactMessageRepository;
import ng.sciCloud.library.service.LoanService;
import ng.sciCloud.library.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@AutoConfigureMockMvc
class LibraryWebFlowTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private LoanService loanService;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ContactMessageRepository messageRepository;

    private RequestPostProcessor asMember(AppUser member) {
        return user(member.getEmail()).roles("USER");
    }

    private RequestPostProcessor asAdmin() {
        return user("admin@library.com").roles("ADMIN");
    }

    @Test
    void anonymousVisitorsAreSentToLoginForProtectedPages() throws Exception {
        for (String path : new String[]{"/books", "/my/loans", "/my/reservations", "/contact", "/admin", "/admin/books"}) {
            mockMvc.perform(get(path))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }
    }

    @Test
    void publicPagesAreOpenToEveryone() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("home"));
        mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
        mockMvc.perform(get("/register")).andExpect(status().isOk()).andExpect(view().name("register"));
        mockMvc.perform(get("/css/style.css")).andExpect(status().isOk());
    }

    @Test
    void homeRedirectsSignedInUsersByRole() throws Exception {
        AppUser member = newMember();

        mockMvc.perform(get("/").with(asAdmin())).andExpect(redirectedUrl("/admin"));
        mockMvc.perform(get("/").with(asMember(member))).andExpect(redirectedUrl("/books"));
    }

    @Test
    void membersCannotOpenAdminPages() throws Exception {
        AppUser member = newMember();
        for (String path : new String[]{"/admin", "/admin/books", "/admin/books/new", "/admin/loans",
                "/admin/fines", "/admin/members", "/admin/messages"}) {
            mockMvc.perform(get(path).with(asMember(member))).andExpect(status().isForbidden());
        }
        mockMvc.perform(post("/admin/books").with(csrf()).with(asMember(member))).andExpect(status().isForbidden());
    }

    @Test
    void adminCannotUseMemberOnlyPages() throws Exception {
        for (String path : new String[]{"/books", "/my/loans", "/my/reservations", "/contact"}) {
            mockMvc.perform(get(path).with(asAdmin())).andExpect(status().isForbidden());
        }
    }

    @Test
    void postsWithoutCsrfTokenAreRejected() throws Exception {
        AppUser member = newMember();
        Book book = newBook(1);

        mockMvc.perform(post("/books/{id}/issue", book.getId()).with(asMember(member)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPagesRender() throws Exception {
        mockMvc.perform(get("/admin").with(asAdmin()))
                .andExpect(status().isOk()).andExpect(view().name("admin/dashboard"))
                .andExpect(content().string(containsString("Admin dashboard")));
        mockMvc.perform(get("/admin/books").with(asAdmin()))
                .andExpect(status().isOk()).andExpect(view().name("admin/books"));
        mockMvc.perform(get("/admin/books/new").with(asAdmin()))
                .andExpect(status().isOk()).andExpect(view().name("admin/book-form"));
        mockMvc.perform(get("/admin/loans").with(asAdmin()))
                .andExpect(status().isOk()).andExpect(view().name("admin/loans"));
        mockMvc.perform(get("/admin/fines").with(asAdmin()))
                .andExpect(status().isOk()).andExpect(view().name("admin/fines"));
        mockMvc.perform(get("/admin/members").with(asAdmin()))
                .andExpect(status().isOk()).andExpect(view().name("admin/members"));
        mockMvc.perform(get("/admin/messages").with(asAdmin()))
                .andExpect(status().isOk()).andExpect(view().name("admin/messages"));
    }

    @Test
    void registrationThenLoginWorks() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "Ada Obi")
                        .param("email", "ada.web@example.com")
                        .param("password", "Secret123")
                        .param("confirmPassword", "Secret123"))
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("success", containsString("Account created")));

        assertThat(userRepository.findByEmailIgnoreCase("ada.web@example.com")).isPresent();

        mockMvc.perform(formLogin().user("ada.web@example.com").password("Secret123"))
                .andExpect(authenticated().withUsername("ada.web@example.com"))
                .andExpect(redirectedUrl("/books"));
    }

    @Test
    void registrationShowsValidationErrors() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "")
                        .param("email", "not-an-email")
                        .param("password", "short")
                        .param("confirmPassword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("form", "fullName", "email", "password", "confirmPassword"));
    }

    @Test
    void registrationRejectsMismatchedPasswordsAndDuplicateEmails() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "Ada")
                        .param("email", "ada.dup@example.com")
                        .param("password", "Secret123")
                        .param("confirmPassword", "Different1"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasErrors("form"));

        AppUser existing = newMember();
        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "Copycat")
                        .param("email", existing.getEmail().toUpperCase())
                        .param("password", "Secret123")
                        .param("confirmPassword", "Secret123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasErrors("form"));
    }

    @Test
    void loginRedirectsByRoleAndRejectsBadCredentials() throws Exception {
        AppUser member = newMember();

        mockMvc.perform(formLogin().user("admin@library.com").password("Admin@123"))
                .andExpect(authenticated()).andExpect(redirectedUrl("/admin"));
        mockMvc.perform(formLogin().user(member.getEmail()).password("Password1"))
                .andExpect(authenticated()).andExpect(redirectedUrl("/books"));
        mockMvc.perform(formLogin().user(member.getEmail()).password("wrong-password"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));
        mockMvc.perform(formLogin().user("nobody@example.com").password("Password1"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void logoutEndsTheSession() throws Exception {
        AppUser member = newMember();

        mockMvc.perform(post("/logout").with(csrf()).with(asMember(member)))
                .andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    void adminCanDisableAndReEnableAMember() throws Exception {
        AppUser member = newMember();

        mockMvc.perform(post("/admin/members/{id}/toggle", member.getId()).with(csrf()).with(asAdmin()))
                .andExpect(redirectedUrl("/admin/members"))
                .andExpect(flash().attribute("success", containsString("disabled")));
        mockMvc.perform(formLogin().user(member.getEmail()).password("Password1"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));

        mockMvc.perform(post("/admin/members/{id}/toggle", member.getId()).with(csrf()).with(asAdmin()))
                .andExpect(flash().attribute("success", containsString("log in again")));
        mockMvc.perform(formLogin().user(member.getEmail()).password("Password1"))
                .andExpect(authenticated());
    }

    @Test
    void adminSeesMembersAndTheirLoanHistory() throws Exception {
        AppUser member = newMember();
        Book book = newBook(1);
        loanService.issueBook(member.getId(), book.getId());

        mockMvc.perform(get("/admin/members").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(member.getEmail())));
        mockMvc.perform(get("/admin/members/{id}", member.getId()).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/member-detail"))
                .andExpect(content().string(containsString(book.getTitle())));
    }

    @Test
    void catalogueListsBooksAndFiltersByCategoryAndSearch() throws Exception {
        AppUser member = newMember();
        Book plain = newBook(1);
        Book special = newBook(1);
        special.setCategory("Poetry");
        special.setTitle("Whispering Reeds");
        special.setAuthor("Ngozi Poet");
        bookRepository.flush();

        mockMvc.perform(get("/books").with(asMember(member)))
                .andExpect(status().isOk())
                .andExpect(view().name("books/catalog"))
                .andExpect(content().string(containsString(plain.getTitle())))
                .andExpect(content().string(containsString("Whispering Reeds")));
        mockMvc.perform(get("/books").param("category", "Poetry").with(asMember(member)))
                .andExpect(model().attribute("books", hasSize(1)))
                .andExpect(content().string(containsString("Whispering Reeds")))
                .andExpect(content().string(not(containsString(plain.getTitle()))));
        mockMvc.perform(get("/books").param("q", "ngozi").with(asMember(member)))
                .andExpect(model().attribute("books", hasSize(1)));
        mockMvc.perform(get("/books").param("q", "whisper").param("category", "Testing").with(asMember(member)))
                .andExpect(model().attribute("books", hasSize(0)))
                .andExpect(content().string(containsString("No books match your search.")));
    }

    @Test
    void memberCanIssueAndReturnABook() throws Exception {
        AppUser member = newMember();
        Book book = newBook(2);

        mockMvc.perform(post("/books/{id}/issue", book.getId()).with(csrf()).with(asMember(member)))
                .andExpect(redirectedUrl("/my/loans"))
                .andExpect(flash().attribute("success", containsString("issued")));
        assertThat(bookRepository.findById(book.getId()).orElseThrow().getAvailableQuantity()).isEqualTo(1);

        mockMvc.perform(get("/my/loans").with(asMember(member)))
                .andExpect(status().isOk())
                .andExpect(view().name("my/loans"))
                .andExpect(content().string(containsString(book.getTitle())))
                .andExpect(content().string(containsString("On loan")));

        Loan loan = loanRepository.findByUserIdOrderByIssueDateDescIdDesc(member.getId()).get(0);
        mockMvc.perform(post("/my/loans/{id}/return", loan.getId()).with(csrf()).with(asMember(member)))
                .andExpect(redirectedUrl("/my/loans"))
                .andExpect(flash().attribute("success", containsString("returned")));
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(bookRepository.findById(book.getId()).orElseThrow().getAvailableQuantity()).isEqualTo(2);
    }

    @Test
    void issueErrorsComeBackAsAFlashMessageOnTheRefererPage() throws Exception {
        AppUser holder = newMember();
        AppUser member = newMember();
        Book book = newBook(1);
        loanService.issueBook(holder.getId(), book.getId());

        mockMvc.perform(post("/books/{id}/issue", book.getId()).with(csrf()).with(asMember(member))
                        .header("Referer", "http://localhost/books?q=Test"))
                .andExpect(redirectedUrl("/books?q=Test"))
                .andExpect(flash().attribute("error", containsString("No copies are available")));
    }

    @Test
    void lateReturnRecordsAFineThatAdminCanMarkPaid() throws Exception {
        AppUser member = newMember();
        Book book = newBook(1);
        Loan loan = newOverdueLoan(member, book, 4);

        mockMvc.perform(get("/my/loans").with(asMember(member)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Overdue")))
                .andExpect(content().string(containsString("so far")));
        mockMvc.perform(get("/admin/loans").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(book.getTitle())))
                .andExpect(content().string(containsString("Overdue")));
        mockMvc.perform(get("/admin/members/{id}", member.getId()).with(asAdmin()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/my/loans/{id}/return", loan.getId()).with(csrf()).with(asMember(member)))
                .andExpect(redirectedUrl("/my/loans"))
                .andExpect(flash().attribute("error", containsString("returned late")));
        assertThat(loan.getFineStatus()).isEqualTo(FineStatus.UNPAID);
        assertThat(loan.getFineAmount()).isEqualByComparingTo("20");

        mockMvc.perform(get("/my/loans").with(asMember(member)))
                .andExpect(content().string(containsString("You owe")));
        mockMvc.perform(get("/admin/fines").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mark as paid")));

        Book another = newBook(1);
        mockMvc.perform(post("/books/{id}/issue", another.getId()).with(csrf()).with(asMember(member)))
                .andExpect(flash().attribute("error", containsString("unpaid fine")));

        mockMvc.perform(post("/admin/fines/{id}/pay", loan.getId()).with(csrf()).with(asAdmin()))
                .andExpect(redirectedUrl("/admin/fines"))
                .andExpect(flash().attribute("success", containsString("paid")));
        assertThat(loan.getFineStatus()).isEqualTo(FineStatus.PAID);
        mockMvc.perform(get("/admin/fines").with(asAdmin()))
                .andExpect(content().string(not(containsString("Mark as paid"))));

        mockMvc.perform(post("/books/{id}/issue", another.getId()).with(csrf()).with(asMember(member)))
                .andExpect(flash().attribute("success", containsString("issued")));
    }

    @Test
    void memberCannotReturnAnotherMembersLoanThroughTheWeb() throws Exception {
        AppUser owner = newMember();
        AppUser other = newMember();
        Loan loan = loanService.issueBook(owner.getId(), newBook(1).getId());

        mockMvc.perform(post("/my/loans/{id}/return", loan.getId()).with(csrf()).with(asMember(other)))
                .andExpect(flash().attribute("error", containsString("does not belong to you")));
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ISSUED);
    }

    @Test
    void memberCanReserveAnIssuedBookAndCancelTheReservation() throws Exception {
        AppUser holder = newMember();
        AppUser member = newMember();
        Book book = newBook(1);
        loanService.issueBook(holder.getId(), book.getId());

        mockMvc.perform(get("/books").with(asMember(member)))
                .andExpect(content().string(containsString("Reserve")));
        mockMvc.perform(post("/books/{id}/reserve", book.getId()).with(csrf()).with(asMember(member)))
                .andExpect(redirectedUrl("/my/reservations"))
                .andExpect(flash().attribute("success", containsString("reserved")));

        mockMvc.perform(get("/my/reservations").with(asMember(member)))
                .andExpect(status().isOk())
                .andExpect(view().name("my/reservations"))
                .andExpect(content().string(containsString(book.getTitle())))
                .andExpect(content().string(containsString("Waiting")));
        mockMvc.perform(get("/books").with(asMember(member)))
                .andExpect(content().string(containsString("Reserved by you")));

        Long reservationId = reservationService.reservationsForUser(member.getId()).get(0).getId();
        mockMvc.perform(post("/my/reservations/{id}/cancel", reservationId).with(csrf()).with(asMember(member)))
                .andExpect(redirectedUrl("/my/reservations"))
                .andExpect(flash().attribute("success", containsString("cancelled")));
        assertThat(reservationService.reservationsForUser(member.getId()).get(0).getStatus())
                .isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void reservingABookThatHasFreeCopiesIsRefused() throws Exception {
        AppUser member = newMember();
        Book book = newBook(2);

        mockMvc.perform(post("/books/{id}/reserve", book.getId()).with(csrf()).with(asMember(member)))
                .andExpect(flash().attribute("error", containsString("available right now")));
    }

    @Test
    void contactMessagesReachTheAdminPanel() throws Exception {
        AppUser member = newMember();

        mockMvc.perform(get("/contact").with(asMember(member)))
                .andExpect(status().isOk()).andExpect(view().name("contact"));
        mockMvc.perform(post("/contact").with(csrf()).with(asMember(member))
                        .param("subject", "Lost library card")
                        .param("message", "I lost my card yesterday, please advise."))
                .andExpect(redirectedUrl("/contact"))
                .andExpect(flash().attribute("success", containsString("sent")));
        assertThat(messageRepository.countByResolvedFalse()).isGreaterThanOrEqualTo(1);

        mockMvc.perform(get("/admin/messages").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Lost library card")))
                .andExpect(content().string(containsString("I lost my card yesterday, please advise.")))
                .andExpect(content().string(containsString(member.getEmail())))
                .andExpect(content().string(containsString("Mark resolved")));

        Long id = messageRepository.findAllByOrderByCreatedAtDesc().get(0).getId();
        mockMvc.perform(post("/admin/messages/{id}/resolve", id).with(csrf()).with(asAdmin()))
                .andExpect(redirectedUrl("/admin/messages"))
                .andExpect(flash().attribute("success", containsString("resolved")));
        mockMvc.perform(get("/admin/messages").with(asAdmin()))
                .andExpect(content().string(containsString("Resolved")))
                .andExpect(content().string(not(containsString("Mark resolved"))));
    }

    @Test
    void contactFormValidatesInput() throws Exception {
        AppUser member = newMember();

        mockMvc.perform(post("/contact").with(csrf()).with(asMember(member))
                        .param("subject", "")
                        .param("message", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("contact"))
                .andExpect(model().attributeHasFieldErrors("form", "subject", "message"));
    }

    @Test
    void adminCanAddEditAndDeleteBooks() throws Exception {
        mockMvc.perform(post("/admin/books").with(csrf()).with(asAdmin())
                        .param("title", "Web Added Book")
                        .param("author", "Web Author")
                        .param("isbn", "978-1-23456-789-7")
                        .param("category", "Web")
                        .param("quantity", "3"))
                .andExpect(redirectedUrl("/admin/books"))
                .andExpect(flash().attribute("success", containsString("added")));

        Book book = bookRepository.findByIsbn("9781234567897").orElseThrow();
        assertThat(book.getTitle()).isEqualTo("Web Added Book");
        assertThat(book.getAvailableQuantity()).isEqualTo(3);

        mockMvc.perform(get("/admin/books").with(asAdmin()))
                .andExpect(content().string(containsString("Web Added Book")));
        mockMvc.perform(get("/admin/books/{id}/edit", book.getId()).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/book-form"))
                .andExpect(content().string(containsString("Web Added Book")));

        mockMvc.perform(post("/admin/books/{id}", book.getId()).with(csrf()).with(asAdmin())
                        .param("title", "Web Edited Book")
                        .param("author", "Web Author")
                        .param("isbn", "9781234567897")
                        .param("category", "Web")
                        .param("quantity", "5"))
                .andExpect(redirectedUrl("/admin/books"))
                .andExpect(flash().attribute("success", containsString("updated")));
        assertThat(book.getTitle()).isEqualTo("Web Edited Book");
        assertThat(book.getTotalQuantity()).isEqualTo(5);

        mockMvc.perform(post("/admin/books/{id}/delete", book.getId()).with(csrf()).with(asAdmin()))
                .andExpect(redirectedUrl("/admin/books"))
                .andExpect(flash().attribute("success", containsString("deleted")));
        assertThat(bookRepository.findById(book.getId())).isEmpty();
    }

    @Test
    void adminBookFormShowsValidationAndBusinessErrors() throws Exception {
        mockMvc.perform(post("/admin/books").with(csrf()).with(asAdmin())
                        .param("title", "")
                        .param("author", "")
                        .param("isbn", "abc")
                        .param("category", "")
                        .param("quantity", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/book-form"))
                .andExpect(model().attributeHasFieldErrors("form", "title", "author", "isbn", "category", "quantity"));

        Book existing = newBook(1);
        mockMvc.perform(post("/admin/books").with(csrf()).with(asAdmin())
                        .param("title", "Clash")
                        .param("author", "Someone")
                        .param("isbn", existing.getIsbn())
                        .param("category", "Misc")
                        .param("quantity", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/book-form"))
                .andExpect(model().attributeHasErrors("form"));
    }

    @Test
    void deletingABookWithHistoryIsRefusedWithAMessage() throws Exception {
        AppUser member = newMember();
        Book book = newBook(1);
        loanService.issueBook(member.getId(), book.getId());

        mockMvc.perform(post("/admin/books/{id}/delete", book.getId()).with(csrf()).with(asAdmin())
                        .header("Referer", "http://localhost/admin/books"))
                .andExpect(redirectedUrl("/admin/books"))
                .andExpect(flash().attribute("error", containsString("loan history")));
        assertThat(bookRepository.findById(book.getId())).isPresent();
    }
}
