package com.veggieshop.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Paging and filtering by role
    Page<User> findByRole(User.Role role, Pageable pageable);

    // Search by name or email (case-insensitive)
    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Pageable pageable);

    // Filter by active status if you add 'active' field in User entity
    // Page<User> findByActive(boolean active, Pageable pageable);
}
