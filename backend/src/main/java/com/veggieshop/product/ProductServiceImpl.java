package com.veggieshop.product;

import com.veggieshop.category.Category;
import com.veggieshop.exception.DuplicateException;
import com.veggieshop.exception.ResourceNotFoundException;
import com.veggieshop.category.CategoryRepository;
import com.veggieshop.order.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductDto.ProductResponse create(ProductDto.ProductCreateRequest request) {
        if (productRepository.existsByName(request.getName())) {
            throw new DuplicateException("Product name already exists");
        }
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
                .active(true)
                .category(category)
                .build();
        Product saved = productRepository.save(product);
        return productMapper.toProductResponse(saved);
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
        return productMapper.toProductResponse(updated);
    }

    @Override
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (orderItemRepository.existsByProductId(id)) {
            if (!product.isActive()) {
                return;
            }
            product.setActive(false);
            productRepository.save(product);
        } else {
            productRepository.deleteById(id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto.ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto.ProductResponse> findAll(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(productMapper::toProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto.ProductResponse> findByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable)
                .map(productMapper::toProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto.ProductResponse> findFeatured(Pageable pageable) {
        return productRepository.findByFeaturedTrueAndActiveTrue(pageable)
                .map(productMapper::toProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto.ProductResponse> findAllIncludingInactive(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(productMapper::toProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto.ProductResponse> findByCategoryIncludingInactive(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(productMapper::toProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto.ProductResponse> searchByName(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable)
                .map(productMapper::toProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto.ProductResponse> filterByPrice(java.math.BigDecimal min, java.math.BigDecimal max, Pageable pageable) {
        return productRepository.findByPriceBetweenAndActiveTrue(min, max, pageable)
                .map(productMapper::toProductResponse);
    }
}
