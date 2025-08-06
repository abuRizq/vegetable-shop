package com.veggieshop.unit.product;

import com.veggieshop.category.Category;
import com.veggieshop.category.CategoryRepository;
import com.veggieshop.exception.DuplicateException;
import com.veggieshop.exception.ResourceNotFoundException;
import com.veggieshop.order.OrderItemRepository;
import com.veggieshop.product.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private AutoCloseable closeable;

    private final Category vegetables = Category.builder().id(1L).name("Vegetables").build();
    private final Category fruits = Category.builder().id(2L).name("Fruits").build();

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // ========== Create ==========
    @Test
    void create_shouldSucceed_whenValid() {
        ProductDto.ProductCreateRequest req = new ProductDto.ProductCreateRequest();
        req.setName("Carrot");
        req.setDescription("Fresh carrots");
        req.setPrice(BigDecimal.valueOf(1.50));
        req.setDiscount(BigDecimal.ZERO);
        req.setFeatured(false);
        req.setCategoryId(1L);
        req.setImageUrl("carrot.jpg");

        when(productRepository.existsByName(req.getName())).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(vegetables));

        Product product = Product.builder()
                .id(10L)
                .name("Carrot")
                .description("Fresh carrots")
                .price(BigDecimal.valueOf(1.50))
                .discount(BigDecimal.ZERO)
                .featured(false)
                .imageUrl("carrot.jpg")
                .soldCount(0L)
                .active(true)
                .category(vegetables)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDto.ProductResponse resp = new ProductDto.ProductResponse();
        resp.setId(10L);
        resp.setName("Carrot");
        resp.setPrice(BigDecimal.valueOf(1.50));
        resp.setCategoryId(1L);
        resp.setCategoryName("Vegetables");
        when(productMapper.toProductResponse(any(Product.class))).thenReturn(resp);

        ProductDto.ProductResponse result = productService.create(req);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Carrot");
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(1.50));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void create_shouldThrowDuplicateException_whenProductNameExists() {
        ProductDto.ProductCreateRequest req = new ProductDto.ProductCreateRequest();
        req.setName("Tomato");
        when(productRepository.existsByName("Tomato")).thenReturn(true);

        assertThrows(DuplicateException.class, () -> productService.create(req));
        verify(productRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowResourceNotFound_whenCategoryMissing() {
        ProductDto.ProductCreateRequest req = new ProductDto.ProductCreateRequest();
        req.setName("Pumpkin");
        req.setCategoryId(99L);
        when(productRepository.existsByName("Pumpkin")).thenReturn(false);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.create(req));
    }

    // ========== Update ==========
    @Test
    void update_shouldSucceed_whenValid() {
        Long id = 5L;
        Product oldProduct = Product.builder().id(id).name("Apple").category(vegetables).build();
        ProductDto.ProductUpdateRequest req = new ProductDto.ProductUpdateRequest();
        req.setName("Green Apple");
        req.setDescription("Juicy and tart");
        req.setPrice(BigDecimal.valueOf(2.25));
        req.setDiscount(BigDecimal.valueOf(0.50));
        req.setFeatured(true);
        req.setCategoryId(2L);
        req.setImageUrl("apple.jpg");

        when(productRepository.findById(id)).thenReturn(Optional.of(oldProduct));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(fruits));

        Product updated = Product.builder()
                .id(id)
                .name("Green Apple")
                .description("Juicy and tart")
                .price(BigDecimal.valueOf(2.25))
                .discount(BigDecimal.valueOf(0.50))
                .featured(true)
                .imageUrl("apple.jpg")
                .soldCount(0L)
                .active(true)
                .category(fruits)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(updated);

        ProductDto.ProductResponse resp = new ProductDto.ProductResponse();
        resp.setId(id);
        resp.setName("Green Apple");
        resp.setCategoryId(2L);
        resp.setCategoryName("Fruits");
        when(productMapper.toProductResponse(any(Product.class))).thenReturn(resp);

        ProductDto.ProductResponse result = productService.update(id, req);

        assertThat(result.getName()).isEqualTo("Green Apple");
        assertThat(result.getCategoryName()).isEqualTo("Fruits");
    }

    @Test
    void update_shouldThrowResourceNotFound_whenProductMissing() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        ProductDto.ProductUpdateRequest req = new ProductDto.ProductUpdateRequest();
        req.setCategoryId(1L);
        assertThrows(ResourceNotFoundException.class, () -> productService.update(999L, req));
    }

    @Test
    void update_shouldThrowResourceNotFound_whenCategoryMissing() {
        ProductDto.ProductUpdateRequest req = new ProductDto.ProductUpdateRequest();
        req.setCategoryId(999L);
        Product oldProduct = Product.builder().id(4L).build();
        when(productRepository.findById(4L)).thenReturn(Optional.of(oldProduct));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.update(4L, req));
    }

    // ========== Delete ==========
    @Test
    void delete_shouldSoftDelete_whenOrderExists() {
        Long id = 8L;
        Product product = Product.builder().id(id).active(true).build();
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(orderItemRepository.existsByProductId(id)).thenReturn(true);

        productService.delete(id);

        assertThat(product.isActive()).isFalse();
        verify(productRepository).save(product);
        verify(productRepository, never()).deleteById(id);
    }

    @Test
    void delete_shouldHardDelete_whenNoOrder() {
        Long id = 3L;
        Product product = Product.builder().id(id).active(true).build();
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(orderItemRepository.existsByProductId(id)).thenReturn(false);

        productService.delete(id);

        verify(productRepository).deleteById(id);
        verify(productRepository, never()).save(any());
    }

    @Test
    void delete_shouldDoNothing_whenInactiveAndOrderExists() {
        Long id = 13L;
        Product product = Product.builder().id(id).active(false).build();
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(orderItemRepository.existsByProductId(id)).thenReturn(true);

        productService.delete(id);

        verify(productRepository, never()).save(any());
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void delete_shouldThrowResourceNotFound_whenProductMissing() {
        when(productRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.delete(404L));
    }

    // ========== FindById ==========
    @Test
    void findById_shouldReturn_whenProductExists() {
        Product product = Product.builder().id(2L).name("Mango").category(fruits).build();
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        ProductDto.ProductResponse resp = new ProductDto.ProductResponse();
        resp.setId(2L); resp.setName("Mango");
        when(productMapper.toProductResponse(product)).thenReturn(resp);

        ProductDto.ProductResponse result = productService.findById(2L);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Mango");
    }

    @Test
    void findById_shouldThrow_whenMissing() {
        when(productRepository.findById(90L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.findById(90L));
    }

    // ========== findAll/findByCategory/findFeatured ==========
    @Test
    void findAll_shouldReturnPagedProducts() {
        Pageable pageable = PageRequest.of(0, 2);
        Product p1 = Product.builder().id(1L).name("Tomato").category(vegetables).build();
        Product p2 = Product.builder().id(2L).name("Banana").category(fruits).build();
        List<Product> list = List.of(p1, p2);
        Page<Product> page = new PageImpl<>(list, pageable, 2);

        when(productRepository.findByActiveTrue(pageable)).thenReturn(page);

        when(productMapper.toProductResponse(p1)).thenReturn(new ProductDto.ProductResponse());
        when(productMapper.toProductResponse(p2)).thenReturn(new ProductDto.ProductResponse());

        Page<ProductDto.ProductResponse> result = productService.findAll(pageable);

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findByCategory_shouldReturnPagedProducts() {
        Pageable pageable = PageRequest.of(0, 1);
        Product p1 = Product.builder().id(1L).name("Cucumber").category(vegetables).build();
        Page<Product> page = new PageImpl<>(List.of(p1), pageable, 1);

        when(productRepository.findByCategoryIdAndActiveTrue(1L, pageable)).thenReturn(page);
        when(productMapper.toProductResponse(p1)).thenReturn(new ProductDto.ProductResponse());

        Page<ProductDto.ProductResponse> result = productService.findByCategory(1L, pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findFeatured_shouldReturnPagedFeaturedProducts() {
        Pageable pageable = PageRequest.of(0, 1);
        Product p1 = Product.builder().id(1L).name("Broccoli").category(vegetables).featured(true).build();
        Page<Product> page = new PageImpl<>(List.of(p1), pageable, 1);

        when(productRepository.findByFeaturedTrueAndActiveTrue(pageable)).thenReturn(page);
        when(productMapper.toProductResponse(p1)).thenReturn(new ProductDto.ProductResponse());

        Page<ProductDto.ProductResponse> result = productService.findFeatured(pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    // ========== searchByName / filterByPrice ==========
    @Test
    void searchByName_shouldReturnMatchedProducts() {
        Pageable pageable = PageRequest.of(0, 2);
        Product p1 = Product.builder().id(1L).name("Carrot").build();
        Page<Product> page = new PageImpl<>(List.of(p1), pageable, 1);

        when(productRepository.findByNameContainingIgnoreCaseAndActiveTrue("carrot", pageable)).thenReturn(page);
        when(productMapper.toProductResponse(p1)).thenReturn(new ProductDto.ProductResponse());

        Page<ProductDto.ProductResponse> result = productService.searchByName("carrot", pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void filterByPrice_shouldReturnFilteredProducts() {
        Pageable pageable = PageRequest.of(0, 2);
        Product p1 = Product.builder().id(1L).name("Apple").price(BigDecimal.valueOf(2.5)).build();
        Product p2 = Product.builder().id(2L).name("Banana").price(BigDecimal.valueOf(1.5)).build();
        Page<Product> page = new PageImpl<>(List.of(p1, p2), pageable, 2);

        when(productRepository.findByPriceBetweenAndActiveTrue(
                BigDecimal.valueOf(1.0), BigDecimal.valueOf(3.0), pageable)).thenReturn(page);

        when(productMapper.toProductResponse(p1)).thenReturn(new ProductDto.ProductResponse());
        when(productMapper.toProductResponse(p2)).thenReturn(new ProductDto.ProductResponse());

        Page<ProductDto.ProductResponse> result = productService.filterByPrice(
                BigDecimal.valueOf(1.0), BigDecimal.valueOf(3.0), pageable);

        assertThat(result.getContent()).hasSize(2);
    }
}
