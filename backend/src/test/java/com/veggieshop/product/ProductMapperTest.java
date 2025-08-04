package com.veggieshop.product;

import com.veggieshop.category.Category;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void shouldMapProductToProductResponse() {
        // Arrange
        Category category = Category.builder()
                .id(99L)
                .name("Fruits")
                .build();

        Product product = Product.builder()
                .id(1L)
                .name("Apple")
                .description("Red apple")
                .price(BigDecimal.valueOf(5.50))
                .discount(BigDecimal.valueOf(1.25))
                .featured(true)
                .soldCount(12L)
                .imageUrl("apple.jpg")
                .category(category)
                .active(true)
                .build();

        // Act
        ProductDto.ProductResponse dto = productMapper.toProductResponse(product);

        // Assert
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Apple");
        assertThat(dto.getDescription()).isEqualTo("Red apple");
        assertThat(dto.getPrice()).isEqualTo(BigDecimal.valueOf(5.50));
        assertThat(dto.getDiscount()).isEqualTo(BigDecimal.valueOf(1.25));
        assertThat(dto.isFeatured()).isTrue();
        assertThat(dto.getSoldCount()).isEqualTo(12L);
        assertThat(dto.getImageUrl()).isEqualTo("apple.jpg");
        assertThat(dto.getCategoryId()).isEqualTo(99L);
        assertThat(dto.getCategoryName()).isEqualTo("Fruits");
        assertThat(dto.isActive()).isTrue();
    }

    @Test
    void shouldHandleNullCategory() {
        // Arrange
        Product product = Product.builder()
                .id(2L)
                .name("Carrot")
                .category(null)
                .build();

        // Act
        ProductDto.ProductResponse dto = productMapper.toProductResponse(product);

        // Assert
        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getCategoryId()).isNull();
        assertThat(dto.getCategoryName()).isNull();
    }
}
