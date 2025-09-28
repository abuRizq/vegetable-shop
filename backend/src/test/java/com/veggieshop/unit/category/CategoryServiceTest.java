package com.veggieshop.unit.category;

import com.veggieshop.category.*;
import com.veggieshop.exception.BadRequestException;
import com.veggieshop.exception.DuplicateException;
import com.veggieshop.exception.ResourceNotFoundException;
import com.veggieshop.product.ProductRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // ============= Create Category =============
    @Test
    void create_shouldSucceed_whenDataIsValid() {
        CategoryDto.CategoryCreateRequest req = new CategoryDto.CategoryCreateRequest();
        req.setName("Nuts");
        req.setDescription("All types of nuts.");

        when(categoryRepository.existsByName("Nuts")).thenReturn(false);

        Category cat = Category.builder().id(10L).name("Nuts").description("All types of nuts.").build();
        when(categoryRepository.save(any(Category.class))).thenReturn(cat);

        CategoryDto.CategoryResponse resp = new CategoryDto.CategoryResponse();
        resp.setId(10L);
        resp.setName("Nuts");
        resp.setDescription("All types of nuts.");
        when(categoryMapper.toCategoryResponse(cat)).thenReturn(resp);

        CategoryDto.CategoryResponse result = categoryService.create(req);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Nuts");
        assertThat(result.getDescription()).isEqualTo("All types of nuts.");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void create_shouldThrowDuplicateException_whenNameExists() {
        CategoryDto.CategoryCreateRequest req = new CategoryDto.CategoryCreateRequest();
        req.setName("Vegetables");

        when(categoryRepository.existsByName("Vegetables")).thenReturn(true);

        assertThrows(DuplicateException.class, () -> categoryService.create(req));
        verify(categoryRepository, never()).save(any());
    }

    // ============= Update Category =============
    @Test
    void update_shouldSucceed_whenCategoryExists() {
        Long id = 1L;
        Category existing = Category.builder().id(id).name("Veg").description("Old desc").build();

        CategoryDto.CategoryUpdateRequest req = new CategoryDto.CategoryUpdateRequest();
        req.setName("Vegetables");
        req.setDescription("Fresh vegetables!");

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));

        Category updated = Category.builder().id(id).name("Vegetables").description("Fresh vegetables!").build();
        when(categoryRepository.save(existing)).thenReturn(updated);

        CategoryDto.CategoryResponse resp = new CategoryDto.CategoryResponse();
        resp.setId(id);
        resp.setName("Vegetables");
        resp.setDescription("Fresh vegetables!");
        when(categoryMapper.toCategoryResponse(updated)).thenReturn(resp);

        CategoryDto.CategoryResponse result = categoryService.update(id, req);

        assertThat(result.getName()).isEqualTo("Vegetables");
        assertThat(result.getDescription()).isEqualTo("Fresh vegetables!");
        verify(categoryRepository).save(existing);
    }

    @Test
    void update_shouldThrowNotFound_whenCategoryMissing() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        CategoryDto.CategoryUpdateRequest req = new CategoryDto.CategoryUpdateRequest();
        req.setName("Vegetables");
        req.setDescription("Fresh vegetables!");
        assertThrows(ResourceNotFoundException.class, () -> categoryService.update(99L, req));
    }

    // ============= Delete Category =============
    @Test
    void delete_shouldSucceed_whenCategoryExistsAndNoProducts() {
        Long id = 2L;
        when(categoryRepository.existsById(id)).thenReturn(true);
        when(productRepository.existsByCategoryId(id)).thenReturn(false);

        categoryService.delete(id);

        verify(categoryRepository).deleteById(id);
    }

    @Test
    void delete_shouldThrowNotFound_whenCategoryMissing() {
        when(categoryRepository.existsById(5L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> categoryService.delete(5L));
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void delete_shouldThrowBadRequest_whenCategoryHasProducts() {
        Long id = 3L;
        when(categoryRepository.existsById(id)).thenReturn(true);
        when(productRepository.existsByCategoryId(id)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.delete(id));
        verify(categoryRepository, never()).deleteById(any());
    }

    // ============= Find By Id =============
    @Test
    void findById_shouldReturnCategory_whenExists() {
        Category cat = Category.builder().id(6L).name("Fruits").description("Desc").build();
        when(categoryRepository.findById(6L)).thenReturn(Optional.of(cat));
        CategoryDto.CategoryResponse resp = new CategoryDto.CategoryResponse();
        resp.setId(6L);
        resp.setName("Fruits");
        resp.setDescription("Desc");
        when(categoryMapper.toCategoryResponse(cat)).thenReturn(resp);

        CategoryDto.CategoryResponse result = categoryService.findById(6L);
        assertThat(result.getName()).isEqualTo("Fruits");
    }

    @Test
    void findById_shouldThrowNotFound_whenMissing() {
        when(categoryRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(404L));
    }

    // ============= Find All =============
    @Test
    void findAll_shouldReturnPagedCategories() {
        Pageable pageable = PageRequest.of(0, 2);
        Category c1 = Category.builder().id(1L).name("Veg").build();
        Category c2 = Category.builder().id(2L).name("Fruit").build();
        List<Category> list = List.of(c1, c2);
        Page<Category> page = new PageImpl<>(list, pageable, 2);

        when(categoryRepository.findAll(pageable)).thenReturn(page);

        when(categoryMapper.toCategoryResponse(c1)).thenReturn(new CategoryDto.CategoryResponse());
        when(categoryMapper.toCategoryResponse(c2)).thenReturn(new CategoryDto.CategoryResponse());

        Page<CategoryDto.CategoryResponse> result = categoryService.findAll(pageable);

        assertThat(result.getContent()).hasSize(2);
    }

    // ============= Search By Name =============
    @Test
    void searchByName_shouldReturnMatchingCategories() {
        Pageable pageable = PageRequest.of(0, 1);
        Category c1 = Category.builder().id(5L).name("Herbs").build();
        Page<Category> page = new PageImpl<>(List.of(c1), pageable, 1);

        when(categoryRepository.findByNameContainingIgnoreCase("her", pageable)).thenReturn(page);
        when(categoryMapper.toCategoryResponse(c1)).thenReturn(new CategoryDto.CategoryResponse());

        Page<CategoryDto.CategoryResponse> result = categoryService.searchByName("her", pageable);

        assertThat(result.getContent()).hasSize(1);
    }
}
