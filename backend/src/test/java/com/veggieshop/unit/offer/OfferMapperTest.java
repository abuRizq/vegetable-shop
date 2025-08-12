package com.veggieshop.unit.offer;

import com.veggieshop.offer.Offer;
import com.veggieshop.offer.OfferDto;
import com.veggieshop.offer.OfferMapper;
import com.veggieshop.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Should map Offer to OfferResponse correctly (all fields)")
    void toOfferResponse_shouldMapAllFieldsCorrectly() {
        // Arrange
        Product product = Product.builder()
                .id(42L)
                .name("Organic Banana")
                .build();

        Offer offer = Offer.builder()
                .id(10L)
                .product(product)
                .discount(new BigDecimal("1.50"))
                .startDate(LocalDate.of(2024, 8, 5))
                .endDate(LocalDate.of(2024, 8, 31))
                .build();

        // Act
        OfferDto.OfferResponse dto = offerMapper.toOfferResponse(offer);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getProductId()).isEqualTo(42L);
        assertThat(dto.getProductName()).isEqualTo("Organic Banana");
        assertThat(dto.getDiscount()).isEqualByComparingTo("1.50");
        assertThat(dto.getStartDate()).isEqualTo(LocalDate.of(2024, 8, 5));
        assertThat(dto.getEndDate()).isEqualTo(LocalDate.of(2024, 8, 31));
    }

    @Test
    @DisplayName("Should handle null Product gracefully")
    void toOfferResponse_shouldHandleNullProduct() {
        // Arrange
        Offer offer = Offer.builder()
                .id(11L)
                .product(null)
                .discount(new BigDecimal("2.00"))
                .startDate(LocalDate.of(2024, 9, 1))
                .endDate(LocalDate.of(2024, 9, 10))
                .build();

        // Act
        OfferDto.OfferResponse dto = offerMapper.toOfferResponse(offer);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getProductId()).isNull();
        assertThat(dto.getProductName()).isNull();
        assertThat(dto.getDiscount()).isEqualByComparingTo("2.00");
        assertThat(dto.getStartDate()).isEqualTo(LocalDate.of(2024, 9, 1));
        assertThat(dto.getEndDate()).isEqualTo(LocalDate.of(2024, 9, 10));
    }

    @Test
    @DisplayName("Should return null when Offer is null")
    void toOfferResponse_shouldReturnNullForNullOffer() {
        // Act
        OfferDto.OfferResponse dto = offerMapper.toOfferResponse(null);

        // Assert
        assertThat(dto).isNull();
    }
}
