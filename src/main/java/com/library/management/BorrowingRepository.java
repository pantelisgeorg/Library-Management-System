package com.library.management;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
    List<Borrowing> findByBookId(Long bookId);
    List<Borrowing> findByUserId(Long userId);
    List<Borrowing> findByStatus(Borrowing.Status status);
    List<Borrowing> findByDueDateBeforeAndStatus(Instant date, Borrowing.Status status);
}