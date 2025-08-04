package com.veggieshop.offer;

import com.veggieshop.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class OfferMapperTest {

    private OfferMapper offerMapper;

    @BeforeEach
    void setUp() {
        offerMapper = Mappers.getMapper(OfferMapper.class);
    }

    @Test
    void shouldMapOfferToOfferResponse() {
        // Arrange
        Product product = Product.builder()
                .id(7L)
                .name("Apple")
                .build();

        Offer offer = Offer.builder()
                .id(42L)
                .product(product)
                .discount(BigDecimal.valueOf(1.5))
                .startDate(LocalDate.of(2025, 8, 1))
                .endDate(LocalDate.of(2025, 8, 31))
                .build();

        // Act
        OfferDto.OfferResponse dto = offerMapper.toOfferResponse(offer);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(42L);
        assertThat(dto.getProductId()).isEqualTo(7L);
        assertThat(dto.getProductName()).isEqualTo("Apple");
        assertThat(dto.getDiscount()).isEqualByComparingTo(BigDecimal.valueOf(1.5));
        assertThat(dto.getStartDate()).isEqualTo(LocalDate.of(2025, 8, 1));
        assertThat(dto.getEndDate()).isEqualTo(LocalDate.of(2025, 8, 31));
    }

    @Test
    void shouldHandleNullOfferGracefully() {
        // Act
        OfferDto.OfferResponse dto = offerMapper.toOfferResponse(null);

        // Assert
        assertThat(dto).isNull();
    }
}
