package com.veggieshop.unit.util;

import com.veggieshop.offer.Offer;
import com.veggieshop.product.Product;
import com.veggieshop.util.PriceCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PriceCalculatorTest {

    @Test
    void calculateFinalPrice_shouldReturnPriceWithoutDiscounts_whenNoOffersAndNoProductDiscount() {
        Product product = Product.builder()
                .price(BigDecimal.valueOf(100))
                .discount(BigDecimal.ZERO)
                .build();

        BigDecimal price = PriceCalculator.calculateFinalPrice(product, List.of(), LocalDate.now());
        assertThat(price).isEqualByComparingTo("100");
    }

    @Test
    void calculateFinalPrice_shouldApplyProductDiscountOnly_whenNoOffers() {
        Product product = Product.builder()
                .price(BigDecimal.valueOf(200))
                .discount(BigDecimal.valueOf(20))
                .build();

        BigDecimal price = PriceCalculator.calculateFinalPrice(product, List.of(), LocalDate.now());
        assertThat(price).isEqualByComparingTo("180");
    }

    @Test
    void calculateFinalPrice_shouldApplyOfferDiscountOnly_whenProductDiscountIsZero() {
        LocalDate today = LocalDate.now();
        Product product = Product.builder()
                .price(BigDecimal.valueOf(50))
                .discount(BigDecimal.ZERO)
                .build();

        Offer offer = Offer.builder()
                .discount(BigDecimal.valueOf(15))
                .startDate(today.minusDays(1))
                .endDate(today.plusDays(1))
                .build();

        BigDecimal price = PriceCalculator.calculateFinalPrice(product, List.of(offer), today);
        assertThat(price).isEqualByComparingTo("35");
    }

    @Test
    void calculateFinalPrice_shouldApplyProductDiscountAndOfferDiscount() {
        LocalDate today = LocalDate.now();
        Product product = Product.builder()
                .price(BigDecimal.valueOf(300))
                .discount(BigDecimal.valueOf(25))
                .build();

        Offer offer = Offer.builder()
                .discount(BigDecimal.valueOf(50))
                .startDate(today.minusDays(1))
                .endDate(today.plusDays(1))
                .build();

        BigDecimal price = PriceCalculator.calculateFinalPrice(product, List.of(offer), today);
        assertThat(price).isEqualByComparingTo("225");
    }

    @Test
    void calculateFinalPrice_shouldUseBestActiveOfferDiscount() {
        LocalDate today = LocalDate.now();
        Product product = Product.builder()
                .price(BigDecimal.valueOf(100))
                .discount(BigDecimal.valueOf(10))
                .build();

        Offer offer1 = Offer.builder()
                .discount(BigDecimal.valueOf(15))
                .startDate(today.minusDays(2))
                .endDate(today.plusDays(2))
                .build();
        Offer offer2 = Offer.builder()
                .discount(BigDecimal.valueOf(25))
                .startDate(today.minusDays(2))
                .endDate(today.plusDays(2))
                .build();

        BigDecimal price = PriceCalculator.calculateFinalPrice(product, List.of(offer1, offer2), today);
        assertThat(price).isEqualByComparingTo("65");
    }

    @Test
    void calculateFinalPrice_shouldIgnoreInactiveOffers() {
        LocalDate today = LocalDate.now();
        Product product = Product.builder()
                .price(BigDecimal.valueOf(80))
                .discount(BigDecimal.valueOf(10))
                .build();

        Offer inactiveOffer = Offer.builder()
                .discount(BigDecimal.valueOf(100))
                .startDate(today.minusDays(10))
                .endDate(today.minusDays(5))
                .build();

        BigDecimal price = PriceCalculator.calculateFinalPrice(product, List.of(inactiveOffer), today);
        assertThat(price).isEqualByComparingTo("70");
    }

    @Test
    void calculateFinalPrice_shouldNeverReturnNegativePrice() {
        LocalDate today = LocalDate.now();
        Product product = Product.builder()
                .price(BigDecimal.valueOf(40))
                .discount(BigDecimal.valueOf(15))
                .build();

        Offer offer = Offer.builder()
                .discount(BigDecimal.valueOf(50))
                .startDate(today.minusDays(1))
                .endDate(today.plusDays(1))
                .build();

        BigDecimal price = PriceCalculator.calculateFinalPrice(product, List.of(offer), today);
        assertThat(price).isEqualByComparingTo("0");
    }

    @Test
    void calculateFinalPrice_shouldTreatNullProductDiscountAsZero() {
        Product product = Product.builder()
                .price(BigDecimal.valueOf(99))
                .discount(null)
                .build();

        BigDecimal price = PriceCalculator.calculateFinalPrice(product, List.of(), LocalDate.now());
        assertThat(price).isEqualByComparingTo("99");
    }
}
