package com.veggieshop.unit.product;

import com.veggieshop.category.Category;
import com.veggieshop.product.Product;
import com.veggieshop.product.ProductDto;
import com.veggieshop.product.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        productMapper = Mappers.getMapper(ProductMapper.class);
    }

    @Test
    void toProductResponse_shouldMapAllFieldsIncludingCategory() {
        // Arrange: build Product with Category
        Category category = Category.builder()
                .id(10L)
                .name("Vegetables")
                .description("All kinds of fresh and organic vegetables.")
                .build();

        Product product = Product.builder()
                .id(5L)
                .name("Fresh Tomato")
                .description("Very fresh and juicy tomatoes")
                .price(BigDecimal.valueOf(2.99))
                .discount(BigDecimal.valueOf(0.50))
                .featured(true)
                .soldCount(25L)
                .imageUrl("https://img.com/tomato.jpg")
                .active(true)
                .category(category)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Act
        ProductDto.ProductResponse dto = productMapper.toProductResponse(product);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(product.getId());
        assertThat(dto.getName()).isEqualTo(product.getName());
        assertThat(dto.getDescription()).isEqualTo(product.getDescription());
        assertThat(dto.getPrice()).isEqualTo(product.getPrice());
        assertThat(dto.getDiscount()).isEqualTo(product.getDiscount());
        assertThat(dto.isFeatured()).isEqualTo(product.isFeatured());
        assertThat(dto.getSoldCount()).isEqualTo(product.getSoldCount());
        assertThat(dto.getImageUrl()).isEqualTo(product.getImageUrl());
        assertThat(dto.isActive()).isEqualTo(product.isActive());
        // Check category mapping
        assertThat(dto.getCategoryId()).isEqualTo(category.getId());
        assertThat(dto.getCategoryName()).isEqualTo(category.getName());
    }

    @Test
    void toProductResponse_shouldReturnNull_whenProductIsNull() {
        // Act & Assert
        assertThat(productMapper.toProductResponse(null)).isNull();
    }
}
