package com.veggieshop.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductDto.ProductResponse create(ProductDto.ProductCreateRequest request);
    ProductDto.ProductResponse update(Long id, ProductDto.ProductUpdateRequest request);
    void delete(Long id);
    ProductDto.ProductResponse findById(Long id);

    // All products (active only)
    Page<ProductDto.ProductResponse> findAll(Pageable pageable);

    // Products by category (active only)
    Page<ProductDto.ProductResponse> findByCategory(Long categoryId, Pageable pageable);

    // Featured products (active only)
    Page<ProductDto.ProductResponse> findFeatured(Pageable pageable);

    // Admin: all products, including inactive
    Page<ProductDto.ProductResponse> findAllIncludingInactive(Pageable pageable);

    // Admin: by category, including inactive
    Page<ProductDto.ProductResponse> findByCategoryIncludingInactive(Long categoryId, Pageable pageable);

    // Filtering/searching
    Page<ProductDto.ProductResponse> searchByName(String name, Pageable pageable);

    Page<ProductDto.ProductResponse> filterByPrice(java.math.BigDecimal min, java.math.BigDecimal max, Pageable pageable);
}
