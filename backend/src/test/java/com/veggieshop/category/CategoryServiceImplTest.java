    package com.veggieshop.category;

    import com.veggieshop.exception.BadRequestException;
    import com.veggieshop.exception.DuplicateException;
    import com.veggieshop.exception.ResourceNotFoundException;
    import com.veggieshop.product.ProductRepository;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.mockito.*;
    import org.springframework.data.domain.*;

    import java.util.List;
    import java.util.Optional;

    import static org.assertj.core.api.Assertions.*;
    import static org.mockito.Mockito.*;

    class CategoryServiceImplTest {

        @Mock
        private CategoryRepository categoryRepository;
        @Mock
        private ProductRepository productRepository;
        @Mock
        private CategoryMapper categoryMapper;

        @InjectMocks
        private CategoryServiceImpl categoryService;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
        }

        // ========== CREATE ==========
        @Test
        void shouldCreateCategory_whenNotExists() {
            CategoryDto.CategoryCreateRequest request = new CategoryDto.CategoryCreateRequest();
            request.setName("Fruits");
            request.setDescription("Fresh fruits");

            when(categoryRepository.existsByName("Fruits")).thenReturn(false);

            Category category = Category.builder()
                    .id(1L)
                    .name("Fruits")
                    .description("Fresh fruits")
                    .build();

            when(categoryRepository.save(any(Category.class))).thenReturn(category);

            CategoryDto.CategoryResponse response = new CategoryDto.CategoryResponse();
            response.setId(1L);
            response.setName("Fruits");

            when(categoryMapper.toCategoryResponse(category)).thenReturn(response);

            CategoryDto.CategoryResponse created = categoryService.create(request);

            assertThat(created).isNotNull();
            assertThat(created.getName()).isEqualTo("Fruits");
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        void shouldThrowDuplicateException_whenCategoryNameExists() {
            CategoryDto.CategoryCreateRequest request = new CategoryDto.CategoryCreateRequest();
            request.setName("Vegetables");
            when(categoryRepository.existsByName("Vegetables")).thenReturn(true);

            assertThatThrownBy(() -> categoryService.create(request))
                    .isInstanceOf(DuplicateException.class)
                    .hasMessageContaining("already exists");
        }

        // ========== UPDATE ==========
        @Test
        void shouldUpdateCategory_whenExists() {
            Long id = 100L;
            CategoryDto.CategoryUpdateRequest request = new CategoryDto.CategoryUpdateRequest();
            request.setName("Herbs");
            request.setDescription("Aromatic herbs");

            Category category = Category.builder()
                    .id(id)
                    .name("Old Name")
                    .description("Old desc")
                    .build();

            when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
            when(categoryRepository.save(category)).thenReturn(category);

            CategoryDto.CategoryResponse response = new CategoryDto.CategoryResponse();
            response.setId(id);
            response.setName("Herbs");

            when(categoryMapper.toCategoryResponse(category)).thenReturn(response);

            CategoryDto.CategoryResponse updated = categoryService.update(id, request);

            assertThat(updated.getName()).isEqualTo("Herbs");
            verify(categoryRepository).save(category);
        }

        @Test
        void shouldThrowResourceNotFound_whenUpdateNonExistingCategory() {
            Long id = 99L;
            CategoryDto.CategoryUpdateRequest request = new CategoryDto.CategoryUpdateRequest();
            request.setName("NotFound");
            when(categoryRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.update(id, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("not found");
        }

        // ========== DELETE ==========
        @Test
        void shouldDeleteCategory_whenNoProducts() {
            Long id = 1L;
            Category category = Category.builder().id(id).name("Veggies").build();
            when(categoryRepository.existsById(id)).thenReturn(true);
            when(productRepository.existsByCategoryId(id)).thenReturn(false);
            doNothing().when(categoryRepository).deleteById(id);

            categoryService.delete(id);

            verify(categoryRepository).deleteById(id);
        }

        @Test
        void shouldThrowResourceNotFound_whenDeleteNonExistingCategory() {
            Long id = 22L;
            when(categoryRepository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> categoryService.delete(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        void shouldThrowBadRequest_whenDeleteCategoryWithProducts() {
            Long id = 2L;
            when(categoryRepository.existsById(id)).thenReturn(true);
            when(productRepository.existsByCategoryId(id)).thenReturn(true);

            assertThatThrownBy(() -> categoryService.delete(id))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("associated products");
        }

        // ========== FIND BY ID ==========
        @Test
        void shouldFindCategoryById_whenExists() {
            Long id = 1L;
            Category category = Category.builder().id(id).name("Herbs").build();
            when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

            CategoryDto.CategoryResponse response = new CategoryDto.CategoryResponse();
            response.setId(id);
            response.setName("Herbs");

            when(categoryMapper.toCategoryResponse(category)).thenReturn(response);

            CategoryDto.CategoryResponse found = categoryService.findById(id);

            assertThat(found).isNotNull().extracting(CategoryDto.CategoryResponse::getName).isEqualTo("Herbs");
        }

        @Test
        void shouldThrowResourceNotFound_whenFindByIdNotFound() {
            Long id = 777L;
            when(categoryRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.findById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("not found");
        }

        // ========== FIND ALL (PAGED) ==========
        @Test
        void shouldReturnPagedCategories() {
            Category c1 = Category.builder().id(1L).name("A").build();
            Category c2 = Category.builder().id(2L).name("B").build();
            Page<Category> page = new PageImpl<>(List.of(c1, c2));
            Pageable pageable = PageRequest.of(0, 10);

            when(categoryRepository.findAll(pageable)).thenReturn(page);

            CategoryDto.CategoryResponse r1 = new CategoryDto.CategoryResponse();
            r1.setId(1L);
            r1.setName("A");
            CategoryDto.CategoryResponse r2 = new CategoryDto.CategoryResponse();
            r2.setId(2L);
            r2.setName("B");

            when(categoryMapper.toCategoryResponse(c1)).thenReturn(r1);
            when(categoryMapper.toCategoryResponse(c2)).thenReturn(r2);

            Page<CategoryDto.CategoryResponse> result = categoryService.findAll(pageable);

            assertThat(result.getContent()).hasSize(2);
        }

        // ========== SEARCH BY NAME (PAGED) ==========
        @Test
        void shouldSearchByNamePaged() {
            Category cat = Category.builder().id(1L).name("Veggie").build();
            Page<Category> page = new PageImpl<>(List.of(cat));
            Pageable pageable = PageRequest.of(0, 5);

            when(categoryRepository.findByNameContainingIgnoreCase("Veg", pageable)).thenReturn(page);

            CategoryDto.CategoryResponse resp = new CategoryDto.CategoryResponse();
            resp.setId(1L);
            resp.setName("Veggie");
            when(categoryMapper.toCategoryResponse(cat)).thenReturn(resp);

            Page<CategoryDto.CategoryResponse> result = categoryService.searchByName("Veg", pageable);

            assertThat(result.getContent()).hasSize(1)
                    .extracting(CategoryDto.CategoryResponse::getName).containsExactly("Veggie");
        }
    }
