package com.library.management;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(User.Role role);

    /** One-time data migration: the LIBRARIAN role was removed — demote any legacy rows to MEMBER. */
    @Modifying
    @Query(value = "UPDATE libdb.users SET role = 'MEMBER' WHERE role = 'LIBRARIAN'", nativeQuery = true)
    int demoteLibrarians();
}
