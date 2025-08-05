package com.veggieshop.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by email (for authentication).
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email already exists (for registration/validation).
     */
    boolean existsByEmail(String email);

    /**
     * Get all users by role (admin page/search).
     */
    Page<User> findByRole(User.Role role, Pageable pageable);

    /**
     * Search by name or email (for user search/autocomplete).
     */
    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Pageable pageable);

    // Add more custom queries if needed
}
