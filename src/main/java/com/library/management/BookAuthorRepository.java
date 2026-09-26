package com.library.management;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookAuthorRepository extends JpaRepository<BookAuthor, Long> {
    List<BookAuthor> findByBookId(Long bookId);
    List<BookAuthor> findByAuthorId(Long authorId);

    // Bulk deletes execute immediately in the current transaction, which
    // reliably removes join-table rows BEFORE the book/author row is deleted.
    @Modifying
    @Query("DELETE FROM BookAuthor ba WHERE ba.id.authorId = :authorId")
    int deleteAllByAuthorId(@Param("authorId") Long authorId);

    @Modifying
    @Query("DELETE FROM BookAuthor ba WHERE ba.id.bookId = :bookId")
    int deleteAllByBookId(@Param("bookId") Long bookId);

    // Native insert for seeding join rows (avoids @MapsId persist quirks)
    @Modifying
    @Query(value = "INSERT INTO libdb.book_authors (author_id, book_id) VALUES (:authorId, :bookId)",
            nativeQuery = true)
    int insertLink(@Param("authorId") Long authorId, @Param("bookId") Long bookId);
}
