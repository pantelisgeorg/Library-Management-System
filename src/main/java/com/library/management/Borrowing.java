package com.library.management;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "borrowings", schema = "libdb")
public class Borrowing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private com.library.management.User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "borrow_date", nullable = false)
    private Timestamp borrowDate;

    @Column(name = "due_date", nullable = false)
    private Timestamp dueDate;

    @Column(name = "return_date")
    private Instant returnDate;

    @Enumerated(EnumType.STRING) // Χρησιμοποιούμε EnumType.STRING αντί για @Lob
    @Column(name = "status", nullable = false)
    private Status status;

    public enum Status {
        BORROWED, RETURNED, OVERDUE
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public com.library.management.User getUser() {
        return user;
    }

    public void setUser(com.library.management.User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Timestamp getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(Timestamp borrowDate) {
        this.borrowDate = borrowDate;
    }

    public Timestamp getDueDate() {
        return dueDate;
    }

    public void setDueDate(Timestamp dueDate) {
        this.dueDate = dueDate;
    }

    public Instant getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Instant returnDate) {
        this.returnDate = returnDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
