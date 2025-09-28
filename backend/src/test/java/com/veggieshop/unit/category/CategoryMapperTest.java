package com.veggieshop.unit.category;

import com.veggieshop.category.Category;
import com.veggieshop.category.CategoryDto;
import com.veggieshop.category.CategoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryMapperTest {

    private CategoryMapper categoryMapper;

    @BeforeEach
    void setUp() {
        categoryMapper = Mappers.getMapper(CategoryMapper.class);
    }

    @Test
    void toCategoryResponse_shouldMapAllFieldsCorrectly() {
        // Arrange
        Category category = Category.builder()
                .id(5L)
                .name("Herbs")
                .description("Fresh herbs for cooking and tea.")
                .build();

        // Act
        CategoryDto.CategoryResponse dto = categoryMapper.toCategoryResponse(category);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(category.getId());
        assertThat(dto.getName()).isEqualTo(category.getName());
        assertThat(dto.getDescription()).isEqualTo(category.getDescription());
    }

    @Test
    void toCategoryResponse_shouldReturnNull_whenCategoryIsNull() {
        // Act & Assert
        assertThat(categoryMapper.toCategoryResponse(null)).isNull();
    }
}
