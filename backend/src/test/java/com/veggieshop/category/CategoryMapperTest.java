package com.veggieshop.category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.*;

class CategoryMapperTest {

    private CategoryMapper categoryMapper;

    @BeforeEach
    void setUp() {
        // Use the MapStruct generated implementation
        categoryMapper = Mappers.getMapper(CategoryMapper.class);
    }

    @Test
    void shouldMapCategoryToCategoryResponse() {
        // Arrange
        Category category = Category.builder()
                .id(123L)
                .name("Vegetables")
                .description("Fresh vegetables")
                .build();

        // Act
        CategoryDto.CategoryResponse response = categoryMapper.toCategoryResponse(category);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(123L);
        assertThat(response.getName()).isEqualTo("Vegetables");
        assertThat(response.getDescription()).isEqualTo("Fresh vegetables");
    }

    @Test
    void shouldReturnNull_WhenCategoryIsNull() {
        // Act
        CategoryDto.CategoryResponse response = categoryMapper.toCategoryResponse(null);

        // Assert
        assertThat(response).isNull();
    }
}
