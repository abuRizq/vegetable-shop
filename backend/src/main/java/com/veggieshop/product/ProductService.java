package com.veggieshop.service;

import com.veggieshop.dto.ProductDto;

import java.util.List;

public interface ProductService {
    ProductDto.ProductResponse create(ProductDto.ProductCreateRequest request);
    ProductDto.ProductResponse update(Long id, ProductDto.ProductUpdateRequest request);
    void delete(Long id);
    ProductDto.ProductResponse findById(Long id);
    List<ProductDto.ProductResponse> findAll();
    List<ProductDto.ProductResponse> findByCategory(Long categoryId);
    List<ProductDto.ProductResponse> findFeatured();
}
