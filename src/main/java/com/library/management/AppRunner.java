package com.library.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.sql.Timestamp;


@Component
@Order(1)
public class AppRunner implements ApplicationRunner {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private BookAuthorRepository bookAuthorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional  // one persistence context: entities stay managed so the
    // ManyToMany collections set below actually flush the join rows at commit
    public void run(ApplicationArguments args) throws Exception {
        // --- Migrations for existing data (run on every startup) ---
        // 1) The LIBRARIAN role was removed from the enum: demote any legacy rows to MEMBER
        //    (otherwise loading them would crash with "No enum constant ... LIBRARIAN").
        userRepository.demoteLibrarians();
        // 2) Passwords used to be stored in plain text: BCrypt-encode them once.
        //    Detected by the "$2" BCrypt prefix — already-encoded passwords are skipped.
        for (User user : userRepository.findAll()) {
            String pw = user.getPassword();
            if (pw != null && !pw.startsWith("$2")) {
                user.setPassword(passwordEncoder.encode(pw));
            }
        }

        // Seed demo data only once (avoid duplicates on every restart)
        if (bookRepository.count() > 0 || userRepository.count() > 0) {
            return;
        }

        // Create Authors
        Author uncleBob = createAuthor(
                "Robert C. Martin",
                "Also known as Uncle Bob, is a software engineer and author."
        );

        Author martinFowler = createAuthor(
                "Martin Fowler",
                "Author of books on software development"
        );

        // Create Books
        Book cleanCoder = createBook(
                "The Clean Coder",
                "9780137081073",
                LocalDate.of(2011, 5, 13),
                "Programming",
                "A Code of Conduct for Professional Programmers",
                "https://example.com/clean-coder.jpg",
                5
        );

        Book refactoring = createBook(
                "Refactoring",
                "9780134757599",
                LocalDate.of(2018, 11, 30),
                "Programming",
                "Improving the Design of Existing Code",
                "https://example.com/refactoring.jpg",
                3
        );

        // Associate authors with books. The join rows are written EXPLICITLY
        // via the BookAuthor entity - mutating the ManyToMany collections only
        // works reliably inside a fully managed persistence context, and the
        // seeded entities may already be detached when repository calls commit.
        cleanCoder.getAuthors().add(uncleBob);
        uncleBob.getBooks().add(cleanCoder);
        refactoring.getAuthors().add(martinFowler);
        martinFowler.getBooks().add(refactoring);

        bookRepository.saveAll(Arrays.asList(cleanCoder, refactoring));

        bookAuthorRepository.insertLink(uncleBob.getId(), cleanCoder.getId());
        bookAuthorRepository.insertLink(martinFowler.getId(), refactoring.getId());

        // Create Users
        User admin = createUser(
                "admin",
                "admin123",
                "admin@library.com",
                "ADMIN"
        );

        User member = createUser(
                "john.doe",
                "user123",
                "john@example.com",
                "MEMBER"
        );

        // Create a borrowing
        createBorrowing(member, cleanCoder);
    }

    private Author createAuthor(String name, String bio) {
        Author author = new Author();
        author.setName(name);
        author.setBio(bio);
        return authorRepository.save(author);
    }

    private Book createBook(String title, String isbn, LocalDate publicationDate,
                            String genre, String summary, String coverUrl, int copies) {
        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setPublicationDate(publicationDate);
        book.setGenre(genre);
        book.setSummary(summary);
        book.setCoverImageUrl(coverUrl);
        book.setTotalCopies(copies);
        book.setAvailableCopies(copies);
        return book;
    }

    private User createUser(String username, String password, String email, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // BCrypt-hashed at seed time
        user.setEmail(email);
        user.setRole(User.Role.valueOf(role));
        return userRepository.save(user);
    }

    private Borrowing createBorrowing(User user, Book book) {
        Borrowing borrowing = new Borrowing();
        borrowing.setUser(user);
        borrowing.setBook(book);

        // Αντί για LocalDateTime.now(), χρησιμοποιούμε Timestamp
        borrowing.setBorrowDate(new Timestamp(System.currentTimeMillis()));

        // Για το due date (14 μέρες μετά)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 14);
        borrowing.setDueDate(new Timestamp(calendar.getTimeInMillis()));

        borrowing.setStatus(Borrowing.Status.BORROWED);

        // Ενημερώνουμε τα διαθέσιμα αντίτυπα
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        return borrowingRepository.save(borrowing);
    }
}