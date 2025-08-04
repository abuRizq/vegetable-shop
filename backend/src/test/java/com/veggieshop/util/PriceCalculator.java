package com.veggieshop.util;

import com.veggieshop.offer.Offer;
import com.veggieshop.product.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PriceCalculatorTest {

    @Test
    void whenNoOffersAndNoProductDiscount_shouldReturnBasePrice() {
        Product product = Product.builder()
                .price(BigDecimal.valueOf(20))
                .discount(BigDecimal.ZERO)
                .build();

        BigDecimal price = PriceCalculator.calculateFinalPrice(product, List.of(), LocalDate.now());

        assertThat(price).isEqualTo(BigDecimal.valueOf(20));
    }

    @Test
    void whenActiveOfferExists_shouldApplyOfferDiscount() {
        Product product = Product.builder()
                .price(BigDecimal.valueOf(100))
                .discount(BigDecimal.ZERO)
                .build();

        Offer offer = Offer.builder()
                .discount(BigDecimal.valueOf(20))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .build();

        BigDecimal price = PriceCalculator.calculateFinalPrice(product, List.of(offer), LocalDate.now());

        assertThat(price).isEqualTo(BigDecimal.valueOf(80));
    }

    @Test
    void whenMultipleActiveOffers_shouldApplyMaxDiscount() {
        Product product = Product.builder()
                .price(BigDecimal.valueOf(50))
                .discount(BigDecimal.ZERO)
                .build();

        Offer offer1 = Offer.builder()
                .discount(BigDecimal.valueOf(5))
                .startDate(LocalDate.now().minusDays(2))
                .endDate(LocalDate.now().plusDays(2))
                .build();

        Offer offer2 = Offer.builder()
                .discount(BigDecimal.valueOf(8))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .build();

        BigDecimal price = PriceCalculator.calculateFinalPrice(product, List.of(offer1, offer2), LocalDate.now());

        assertThat(price).isEqualTo(BigDecimal.valueOf(42));
    }

    @Test
    void whenOfferNotActive_shouldNotApplyDiscount() {
        Product product = Product.builder()
                .price(BigDecimal.valueOf(60))
                .discount(BigDecimal.ZERO)
                .build();

        Offer offer = Offer.builder()
                .discount(BigDecimal.valueOf(10))
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().minusDays(5))
                .build();

        BigDecimal price = PriceCalculator.calculateFinalPrice(product, List.of(offer), LocalDate.now());

        assertThat(price).isEqualTo(BigDecimal.valueOf(60));
    }

    @Test
    void whenProductHasDiscountAndNoActiveOffers_shouldApplyProductDiscount() {
        Product product = Product.builder()
                .price(BigDecimal.valueOf(100))
                .discount(BigDecimal.valueOf(15))
                .build();

        BigDecimal price = PriceCalculator.calculateFinalPrice(product, List.of(), LocalDate.now());

        assertThat(price).isEqualTo(BigDecimal.valueOf(85));
    }

    @Test
    void whenProductAndOfferDiscountExist_shouldApplyMaxDiscount() {
        Product product = Product.builder()
                .price(BigDecimal.valueOf(80))
                .discount(BigDecimal.valueOf(5))
                .build();

        Offer offer = Offer.builder()
                .discount(BigDecimal.valueOf(10))
                .startDate(LocalDate.now().minusDays(2))
                .endDate(LocalDate.now().plusDays(2))
                .build();

        BigDecimal price = PriceCalculator.calculateFinalPrice(product, List.of(offer), LocalDate.now());

        assertThat(price).isEqualTo(BigDecimal.valueOf(70));
    }

    @Test
    void whenDiscountGreaterThanPrice_shouldReturnZero() {
        Product product = Product.builder()
                .price(BigDecimal.valueOf(10))
                .discount(BigDecimal.valueOf(12))
                .build();

        BigDecimal price = PriceCalculator.calculateFinalPrice(product, List.of(), LocalDate.now());

        assertThat(price).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void whenNullDiscounts_shouldWorkAsZeroDiscount() {
        Product product = Product.builder()
                .price(BigDecimal.valueOf(50))
                .discount(null)
                .build();

        Offer offer = Offer.builder()
                .discount(null)
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .build();

        BigDecimal price = PriceCalculator.calculateFinalPrice(product, List.of(offer), LocalDate.now());

        assertThat(price).isEqualTo(BigDecimal.valueOf(50));
    }
}
