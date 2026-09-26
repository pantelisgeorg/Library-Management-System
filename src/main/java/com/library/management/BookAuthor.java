package com.library.management;

import jakarta.persistence.*;

@Entity
@Table(name = "book_authors", schema = "libdb")
public class BookAuthor {
    @EmbeddedId
    private BookAuthorId id;

    @MapsId("bookId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private com.library.management.Book book;

    @MapsId("authorId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    public BookAuthorId getId() {
        return id;
    }

    public void setId(BookAuthorId id) {
        this.id = id;
    }

    public com.library.management.Book getBook() {
        return book;
    }

    public void setBook(com.library.management.Book book) {
        this.book = book;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

}