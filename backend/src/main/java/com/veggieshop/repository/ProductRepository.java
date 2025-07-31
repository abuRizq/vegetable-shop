package com.veggieshop.repository;

import com.veggieshop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // All products (admin)
    List<Product> findAll();

    // Only active products (public, users)
    List<Product> findByActiveTrue();

    // Only active products in a specific category (public, users)
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);

    // All products in a specific category (admin)
    List<Product> findByCategoryId(Long categoryId);

    // Only active featured products (public, users)
    List<Product> findByFeaturedTrueAndActiveTrue();

    // All featured products, active or inactive (admin)
    List<Product> findByFeaturedTrue();

    // Prevent duplicate product names
    boolean existsByName(String name);
}
