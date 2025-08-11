package com.veggieshop.util;

import com.veggieshop.offer.Offer;
import com.veggieshop.product.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PriceCalculator {

    /**
     * Calculates the final price of a product, given its offers and its base discount.
     *
     * @param product The product entity (must not be null).
     * @param offers List of all offers related to the product (can be empty).
     * @param date Calculation date (usually LocalDate.now(), but allows future use cases).
     * @return Final price after applying discounts and offers (not less than zero).
     */
    public static BigDecimal calculateFinalPrice(Product product, List<Offer> offers, LocalDate date) {
        BigDecimal price = product.getPrice();
        BigDecimal productDiscount = product.getDiscount() != null ? product.getDiscount() : BigDecimal.ZERO;

        // 1. Apply product's own discount
        BigDecimal discountedPrice = price.subtract(productDiscount);

        // 2. Find the best valid offer (currently: max discount among active offers)
        Optional<Offer> bestActiveOffer = offers.stream()
                .filter(o -> !date.isBefore(o.getStartDate()) && !date.isAfter(o.getEndDate()))
                .max((a, b) -> a.getDiscount().compareTo(b.getDiscount())); // Use the biggest discount if multiple offers

        if (bestActiveOffer.isPresent()) {
            discountedPrice = discountedPrice.subtract(bestActiveOffer.get().getDiscount());
        }

        // 3. Never allow negative price
        return discountedPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : discountedPrice;
    }
}
