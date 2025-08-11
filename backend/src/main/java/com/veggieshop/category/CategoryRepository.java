package com.veggieshop.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    // For search/filtering by name (contains, ignore case)
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
