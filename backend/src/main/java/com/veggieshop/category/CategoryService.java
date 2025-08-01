package com.veggieshop.category;

import java.util.List;

public interface CategoryService {
    CategoryDto.CategoryResponse create(CategoryDto.CategoryCreateRequest request);
    CategoryDto.CategoryResponse update(Long id, CategoryDto.CategoryUpdateRequest request);
    void delete(Long id);
    CategoryDto.CategoryResponse findById(Long id);
    List<CategoryDto.CategoryResponse> findAll();
}
