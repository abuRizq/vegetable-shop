package com.veggieshop.service;

import com.veggieshop.dto.ProductDto;
import com.veggieshop.entity.Category;
import com.veggieshop.entity.Product;
import com.veggieshop.exception.ResourceNotFoundException;
import com.veggieshop.repository.CategoryRepository;
import com.veggieshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductDto.ProductResponse create(ProductDto.ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .discount(request.getDiscount() != null ? request.getDiscount() : java.math.BigDecimal.ZERO)
                .featured(request.isFeatured())
                .imageUrl(request.getImageUrl())
                .soldCount(0L)
                .category(category)
                .build();
        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    @Override
    public ProductDto.ProductResponse update(Long id, ProductDto.ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscount(request.getDiscount());
        product.setFeatured(request.isFeatured());
        product.setCategory(category);
        product.setImageUrl(request.getImageUrl());
        Product updated = productRepository.save(product);
        return mapToResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto.ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto.ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto.ProductResponse> findByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto.ProductResponse> findFeatured() {
        return productRepository.findByFeaturedTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProductDto.ProductResponse mapToResponse(Product product) {
        ProductDto.ProductResponse dto = new ProductDto.ProductResponse();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setDiscount(product.getDiscount());
        dto.setFeatured(product.isFeatured());
        dto.setSoldCount(product.getSoldCount());
        dto.setImageUrl(product.getImageUrl());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        return dto;
    }
}
