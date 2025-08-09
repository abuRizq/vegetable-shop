package com.veggieshop.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryDto.CategoryResponse create(CategoryDto.CategoryCreateRequest request);
    CategoryDto.CategoryResponse update(Long id, CategoryDto.CategoryUpdateRequest request);
    void delete(Long id);
    CategoryDto.CategoryResponse findById(Long id);

    // Pagination, sorting, filtering
    Page<CategoryDto.CategoryResponse> findAll(Pageable pageable);
    Page<CategoryDto.CategoryResponse> searchByName(String name, Pageable pageable);
}
