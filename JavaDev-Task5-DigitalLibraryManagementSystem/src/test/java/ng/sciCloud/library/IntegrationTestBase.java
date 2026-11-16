package ng.sciCloud.library;

import ng.sciCloud.library.model.AppUser;
import ng.sciCloud.library.model.Book;
import ng.sciCloud.library.model.Loan;
import ng.sciCloud.library.model.LoanStatus;
import ng.sciCloud.library.model.Role;
import ng.sciCloud.library.repository.BookRepository;
import ng.sciCloud.library.repository.LoanRepository;
import ng.sciCloud.library.repository.ReservationRepository;
import ng.sciCloud.library.repository.UserRepository;
import ng.sciCloud.library.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTestBase {

    private static final AtomicInteger SEQ = new AtomicInteger();

    @Autowired
    protected MemberService memberService;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected BookRepository bookRepository;
    @Autowired
    protected LoanRepository loanRepository;
    @Autowired
    protected ReservationRepository reservationRepository;

    protected AppUser newMember() {
        int n = SEQ.incrementAndGet();
        return memberService.createUser("Member " + n, "member" + n + "@test.com", "Password1", Role.USER);
    }

    protected Book newBook(int copies) {
        int n = SEQ.incrementAndGet();
        Book book = new Book();
        book.setTitle("Test Book " + n);
        book.setAuthor("Test Author " + n);
        book.setIsbn(String.format("97800000%05d", n));
        book.setCategory("Testing");
        book.setTotalQuantity(copies);
        book.setAvailableQuantity(copies);
        return bookRepository.save(book);
    }

    protected Loan newOverdueLoan(AppUser member, Book book, int daysLate) {
        LocalDate today = LocalDate.now();
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setUser(member);
        loan.setIssueDate(today.minusDays(14L + daysLate));
        loan.setDueDate(today.minusDays(daysLate));
        loan.setStatus(LoanStatus.ISSUED);
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        return loanRepository.save(loan);
    }
}
