package com.library.management;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "books", schema = "libdb")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "isbn", nullable = false, length = 13)
    private String isbn;

    @Column(name = "publication_date", nullable = false)
    private LocalDate publicationDate;

    @Column(name = "genre", nullable = false, length = 50)
    private String genre;

    // NOTE: no @Lob - on PostgreSQL it triggers the large-object API which
    // fails outside transactions; columnDefinition="text" is the portable type.
    @Column(columnDefinition = "text")
    private String summary;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @ColumnDefault("0")
    @Column(name = "total_copies", nullable = false)
    private Integer totalCopies;

    @ColumnDefault("0")
    @Column(name = "available_copies", nullable = false)
    private Integer availableCopies;

    @ManyToMany(mappedBy = "books")
    private Set<Author> authors = new LinkedHashSet<>();

    @OneToMany(mappedBy = "book")
    private Set<com.library.management.Borrowing> borrowings = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public Integer getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }

    public Integer getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
    }

    public Set<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }

    public Set<com.library.management.Borrowing> getBorrowings() {
        return borrowings;
    }

    public void setBorrowings(Set<com.library.management.Borrowing> borrowings) {
        this.borrowings = borrowings;
    }

}