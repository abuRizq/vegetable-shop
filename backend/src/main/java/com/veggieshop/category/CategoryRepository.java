package com.veggieshop.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    // Exact match (used by the seeder)
    Optional<Category> findByName(String name);

    // For search/filtering by name (contains, ignore case)
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
