package com.veggieshop.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // All products, paginated and sorted (admin)
    Page<Product> findAll(Pageable pageable);

    // Only active products (public, users)
    Page<Product> findByActiveTrue(Pageable pageable);

    // Only active products in a specific category (public, users)
    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    // All products in a specific category (admin)
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    // Only active featured products (public, users)
    Page<Product> findByFeaturedTrueAndActiveTrue(Pageable pageable);

    // All featured products, active or inactive (admin)
    Page<Product> findByFeaturedTrue(Pageable pageable);

    // Search by name containing (case-insensitive)
    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    // Filter by price range (active products)
    Page<Product> findByPriceBetweenAndActiveTrue(java.math.BigDecimal min, java.math.BigDecimal max, Pageable pageable);

    // Prevent duplicate product names
    boolean existsByName(String name);

    boolean existsByCategoryId(Long categoryId);
}
