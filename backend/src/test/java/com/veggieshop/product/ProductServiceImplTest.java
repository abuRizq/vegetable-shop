package com.veggieshop.product;

import com.veggieshop.category.Category;
import com.veggieshop.category.CategoryRepository;
import com.veggieshop.exception.DuplicateException;
import com.veggieshop.exception.ResourceNotFoundException;
import com.veggieshop.order.OrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // CREATE
    @Test
    void shouldCreateProductSuccessfully() {
        ProductDto.ProductCreateRequest request = new ProductDto.ProductCreateRequest();
        request.setName("Tomato");
        request.setPrice(BigDecimal.valueOf(2.5));
        request.setCategoryId(100L);

        when(productRepository.existsByName("Tomato")).thenReturn(false);
        Category category = Category.builder().id(100L).name("Veggies").build();
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(category));
        Product product = Product.builder().name("Tomato").price(BigDecimal.valueOf(2.5)).category(category).build();
        Product saved = Product.builder().id(55L).name("Tomato").price(BigDecimal.valueOf(2.5)).category(category).build();
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductDto.ProductResponse response = new ProductDto.ProductResponse();
        response.setId(55L);
        when(productMapper.toProductResponse(saved)).thenReturn(response);

        ProductDto.ProductResponse result = productService.create(request);
        assertThat(result.getId()).isEqualTo(55L);

        verify(productRepository).existsByName("Tomato");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldThrowOnDuplicateName() {
        ProductDto.ProductCreateRequest request = new ProductDto.ProductCreateRequest();
        request.setName("Tomato");
        when(productRepository.existsByName("Tomato")).thenReturn(true);
        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    void shouldThrowWhenCategoryNotFoundOnCreate() {
        ProductDto.ProductCreateRequest request = new ProductDto.ProductCreateRequest();
        request.setName("Tomato");
        request.setCategoryId(200L);
        when(productRepository.existsByName("Tomato")).thenReturn(false);
        when(categoryRepository.findById(200L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    // UPDATE
    @Test
    void shouldUpdateProductSuccessfully() {
        ProductDto.ProductUpdateRequest request = new ProductDto.ProductUpdateRequest();
        request.setName("Cucumber");
        request.setPrice(BigDecimal.valueOf(3.2));
        request.setCategoryId(101L);

        Product product = Product.builder().id(44L).name("OldName").build();
        Category category = Category.builder().id(101L).name("Salad").build();

        when(productRepository.findById(44L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(101L)).thenReturn(Optional.of(category));

        Product updated = Product.builder().id(44L).name("Cucumber").price(BigDecimal.valueOf(3.2)).category(category).build();
        when(productRepository.save(any(Product.class))).thenReturn(updated);

        ProductDto.ProductResponse response = new ProductDto.ProductResponse();
        response.setId(44L);
        response.setName("Cucumber");
        when(productMapper.toProductResponse(updated)).thenReturn(response);

        ProductDto.ProductResponse result = productService.update(44L, request);
        assertThat(result.getId()).isEqualTo(44L);
        assertThat(result.getName()).isEqualTo("Cucumber");
    }

    @Test
    void shouldThrowWhenProductNotFoundOnUpdate() {
        when(productRepository.findById(66L)).thenReturn(Optional.empty());
        ProductDto.ProductUpdateRequest request = new ProductDto.ProductUpdateRequest();
        request.setCategoryId(99L);
        assertThatThrownBy(() -> productService.update(66L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void shouldThrowWhenCategoryNotFoundOnUpdate() {
        ProductDto.ProductUpdateRequest request = new ProductDto.ProductUpdateRequest();
        request.setCategoryId(42L);

        Product product = Product.builder().id(88L).name("Eggplant").build();
        when(productRepository.findById(88L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(88L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    // DELETE
    @Test
    void shouldDeleteProductIfNoOrders() {
        Product product = Product.builder().id(12L).active(true).build();
        when(productRepository.findById(12L)).thenReturn(Optional.of(product));
        when(orderItemRepository.existsByProductId(12L)).thenReturn(false);

        productService.delete(12L);

        verify(productRepository).deleteById(12L);
    }

    @Test
    void shouldSoftDeleteProductIfHasOrders() {
        Product product = Product.builder().id(20L).active(true).build();
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));
        when(orderItemRepository.existsByProductId(20L)).thenReturn(true);

        productService.delete(20L);

        assertThat(product.isActive()).isFalse();
        verify(productRepository).save(product);
    }

    @Test
    void shouldNotSaveIfAlreadyInactiveWhenSoftDelete() {
        Product product = Product.builder().id(21L).active(false).build();
        when(productRepository.findById(21L)).thenReturn(Optional.of(product));
        when(orderItemRepository.existsByProductId(21L)).thenReturn(true);

        productService.delete(21L);

        verify(productRepository, never()).save(any());
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void shouldThrowWhenProductNotFoundOnDelete() {
        when(productRepository.findById(77L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> productService.delete(77L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    // FIND BY ID
    @Test
    void shouldFindProductById() {
        Product product = Product.builder().id(7L).build();
        ProductDto.ProductResponse resp = new ProductDto.ProductResponse();
        resp.setId(7L);

        when(productRepository.findById(7L)).thenReturn(Optional.of(product));
        when(productMapper.toProductResponse(product)).thenReturn(resp);

        ProductDto.ProductResponse result = productService.findById(7L);
        assertThat(result.getId()).isEqualTo(7L);
    }

    @Test
    void shouldThrowWhenProductNotFoundById() {
        when(productRepository.findById(88L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> productService.findById(88L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    // FIND ALL, BY CATEGORY, FEATURED, ETC.
    @Test
    void shouldFindAllProducts() {
        Product product = Product.builder().id(1L).build();
        Page<Product> page = new PageImpl<>(List.of(product));
        Pageable pageable = PageRequest.of(0, 1);
        ProductDto.ProductResponse resp = new ProductDto.ProductResponse();
        resp.setId(1L);

        when(productRepository.findByActiveTrue(pageable)).thenReturn(page);
        when(productMapper.toProductResponse(product)).thenReturn(resp);

        Page<ProductDto.ProductResponse> result = productService.findAll(pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void shouldFindByCategory() {
        Product product = Product.builder().id(4L).build();
        Page<Product> page = new PageImpl<>(List.of(product));
        Pageable pageable = PageRequest.of(0, 1);
        ProductDto.ProductResponse resp = new ProductDto.ProductResponse();
        resp.setId(4L);

        when(productRepository.findByCategoryIdAndActiveTrue(3L, pageable)).thenReturn(page);
        when(productMapper.toProductResponse(product)).thenReturn(resp);

        Page<ProductDto.ProductResponse> result = productService.findByCategory(3L, pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(4L);
    }

    @Test
    void shouldFindFeaturedProducts() {
        Product product = Product.builder().id(5L).build();
        Page<Product> page = new PageImpl<>(List.of(product));
        Pageable pageable = PageRequest.of(0, 1);
        ProductDto.ProductResponse resp = new ProductDto.ProductResponse();
        resp.setId(5L);

        when(productRepository.findByFeaturedTrueAndActiveTrue(pageable)).thenReturn(page);
        when(productMapper.toProductResponse(product)).thenReturn(resp);

        Page<ProductDto.ProductResponse> result = productService.findFeatured(pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(5L);
    }

    @Test
    void shouldFindAllIncludingInactive() {
        Product product = Product.builder().id(2L).build();
        Page<Product> page = new PageImpl<>(List.of(product));
        Pageable pageable = PageRequest.of(0, 1);
        ProductDto.ProductResponse resp = new ProductDto.ProductResponse();
        resp.setId(2L);

        when(productRepository.findAll(pageable)).thenReturn(page);
        when(productMapper.toProductResponse(product)).thenReturn(resp);

        Page<ProductDto.ProductResponse> result = productService.findAllIncludingInactive(pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindByCategoryIncludingInactive() {
        Product product = Product.builder().id(2L).build();
        Page<Product> page = new PageImpl<>(List.of(product));
        Pageable pageable = PageRequest.of(0, 1);
        ProductDto.ProductResponse resp = new ProductDto.ProductResponse();
        resp.setId(2L);

        when(productRepository.findByCategoryId(9L, pageable)).thenReturn(page);
        when(productMapper.toProductResponse(product)).thenReturn(resp);

        Page<ProductDto.ProductResponse> result = productService.findByCategoryIncludingInactive(9L, pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldSearchByName() {
        Product product = Product.builder().id(3L).build();
        Page<Product> page = new PageImpl<>(List.of(product));
        Pageable pageable = PageRequest.of(0, 1);
        ProductDto.ProductResponse resp = new ProductDto.ProductResponse();
        resp.setId(3L);

        when(productRepository.findByNameContainingIgnoreCaseAndActiveTrue("ap", pageable)).thenReturn(page);
        when(productMapper.toProductResponse(product)).thenReturn(resp);

        Page<ProductDto.ProductResponse> result = productService.searchByName("ap", pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFilterByPrice() {
        Product product = Product.builder().id(6L).build();
        Page<Product> page = new PageImpl<>(List.of(product));
        Pageable pageable = PageRequest.of(0, 1);
        ProductDto.ProductResponse resp = new ProductDto.ProductResponse();
        resp.setId(6L);

        when(productRepository.findByPriceBetweenAndActiveTrue(BigDecimal.ONE, BigDecimal.TEN, pageable)).thenReturn(page);
        when(productMapper.toProductResponse(product)).thenReturn(resp);

        Page<ProductDto.ProductResponse> result = productService.filterByPrice(BigDecimal.ONE, BigDecimal.TEN, pageable);
        assertThat(result.getContent()).hasSize(1);
    }
}
