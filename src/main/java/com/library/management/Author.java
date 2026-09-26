package com.library.management;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "authors", schema = "libdb")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // NOTE: no @Lob - on PostgreSQL it triggers the large-object API which
    // fails outside transactions; columnDefinition="text" is the portable type.
    @Column(columnDefinition = "text")
    private String bio;

    @ManyToMany
    @JoinTable(name = "book_authors",
            joinColumns = @JoinColumn(name = "author_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id"))
    private Set<com.library.management.Book> books = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Set<com.library.management.Book> getBooks() {
        return books;
    }

    public void setBooks(Set<com.library.management.Book> books) {
        this.books = books;
    }

}