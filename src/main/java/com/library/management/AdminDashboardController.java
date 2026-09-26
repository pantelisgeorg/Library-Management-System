package com.library.management;

import com.library.management.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.time.LocalDate;
import java.sql.Timestamp;
import java.util.Calendar;

@Controller
@Transactional
public class AdminDashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private BookAuthorRepository bookAuthorRepository;

    @GetMapping("/")
    public String index(Model model) {
        // Basic statistics for cards
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalBooks", bookRepository.count());
        model.addAttribute("totalAuthors", authorRepository.count());
        model.addAttribute("activeBorrowings",
                borrowingRepository.findByStatus(Borrowing.Status.BORROWED).size());

        return "index"; // Θα χρησιμοποιήσει το index.html του SB Admin
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/tables")
    public String tables(Model model) {
        // Data for DataTables
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("books", bookRepository.findAll());
        model.addAttribute("authors", authorRepository.findAll());
        model.addAttribute("borrowings", borrowingRepository.findAll());
        // For the "Borrow" dialog: members and books with available copies
        model.addAttribute("members", userRepository.findByRole(User.Role.MEMBER));
        model.addAttribute("availableBooks", bookRepository.findAll().stream()
                .filter(b -> b.getAvailableCopies() != null && b.getAvailableCopies() > 0)
                .toList());

        return "tables"; // Θα χρησιμοποιήσει το tables.html του SB Admin
    }

    @GetMapping("/charts")
    public String charts(Model model) {
        // Data for charts
        model.addAttribute("borrowingsByStatus",
                borrowingRepository.findByStatus(Borrowing.Status.BORROWED));
        model.addAttribute("usersByRole",
                userRepository.findByRole(User.Role.MEMBER));

        return "charts"; // Θα χρησιμοποιήσει το charts.html του SB Admin
    }

    // ==================== Books ====================

    @PostMapping("/books/add")
    public String addBook(@RequestParam String title,
                          @RequestParam String isbn,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publicationDate,
                          @RequestParam String genre,
                          @RequestParam(required = false) String summary,
                          @RequestParam Integer totalCopies,
                          RedirectAttributes redirectAttributes) {
        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setPublicationDate(publicationDate);
        book.setGenre(genre);
        book.setSummary(summary);
        book.setTotalCopies(totalCopies);
        book.setAvailableCopies(totalCopies);
        bookRepository.save(book);
        redirectAttributes.addFlashAttribute("message", "Book \"" + title + "\" added");
        return "redirect:/tables";
    }

    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book != null) {
            // Delete the book's borrow history first (FK constraint)
            borrowingRepository.deleteAll(borrowingRepository.findByBookId(id));
            // Remove the book's author associations explicitly (bulk delete -
            // mutating the ManyToMany collection before removing the owner
            // does not reliably queue the join-row deletions)
            bookAuthorRepository.deleteAllByBookId(id);
            bookRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Book \"" + book.getTitle() + "\" deleted");
        }
        return "redirect:/tables";
    }

    // ==================== Authors ====================

    @PostMapping("/authors/add")
    public String addAuthor(@RequestParam String name,
                            @RequestParam(required = false) String bio,
                            RedirectAttributes redirectAttributes) {
        Author author = new Author();
        author.setName(name);
        author.setBio(bio);
        authorRepository.save(author);
        redirectAttributes.addFlashAttribute("message", "Author \"" + name + "\" added");
        return "redirect:/tables";
    }

    @PostMapping("/authors/{id}/delete")
    public String deleteAuthor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Author author = authorRepository.findById(id).orElse(null);
        if (author != null) {
            // Remove the author's book associations explicitly (bulk delete)
            bookAuthorRepository.deleteAllByAuthorId(id);
            authorRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Author \"" + author.getName() + "\" deleted");
        }
        return "redirect:/tables";
    }

    // ==================== Users ====================

    @PostMapping("/users/add")
    public String addUser(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String email,
                          @RequestParam String role,
                          RedirectAttributes redirectAttributes) {
        if (userRepository.findByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Username \"" + username + "\" already exists");
            return "redirect:/tables";
        }
        if (userRepository.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Email \"" + email + "\" already exists");
            return "redirect:/tables";
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setRole(User.Role.valueOf(role));
        user.setCreatedAt(Instant.now());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("message", "User \"" + username + "\" added");
        return "redirect:/tables";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            // Delete the user's borrow history first (FK constraint)
            borrowingRepository.deleteAll(borrowingRepository.findByUserId(id));
            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "User \"" + user.getUsername() + "\" deleted");
        }
        return "redirect:/tables";
    }

    // ==================== Borrowings ====================

    @PostMapping("/borrowings/add")
    public String borrowBook(@RequestParam Long userId,
                             @RequestParam Long bookId,
                             RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(userId).orElse(null);
        Book book = bookRepository.findById(bookId).orElse(null);
        if (user == null || book == null) {
            redirectAttributes.addFlashAttribute("error", "Invalid user or book");
            return "redirect:/tables";
        }
        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            redirectAttributes.addFlashAttribute("error", "No available copies of \"" + book.getTitle() + "\"");
            return "redirect:/tables";
        }
        Borrowing borrowing = new Borrowing();
        borrowing.setUser(user);
        borrowing.setBook(book);
        borrowing.setBorrowDate(new Timestamp(System.currentTimeMillis()));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 14); // due in 14 days
        borrowing.setDueDate(new Timestamp(calendar.getTimeInMillis()));
        borrowing.setStatus(Borrowing.Status.BORROWED);
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
        borrowingRepository.save(borrowing);
        redirectAttributes.addFlashAttribute("message",
                "\"" + book.getTitle() + "\" borrowed by " + user.getUsername());
        return "redirect:/tables";
    }

    @PostMapping("/borrowings/{id}/return")
    public String returnBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Borrowing borrowing = borrowingRepository.findById(id).orElse(null);
        if (borrowing != null && borrowing.getStatus() == Borrowing.Status.BORROWED) {
            borrowing.setReturnDate(Instant.now());
            borrowing.setStatus(Borrowing.Status.RETURNED);
            Book book = borrowing.getBook();
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookRepository.save(book);
            borrowingRepository.save(borrowing);
            redirectAttributes.addFlashAttribute("message", "\"" + book.getTitle() + "\" returned");
        }
        return "redirect:/tables";
    }
}