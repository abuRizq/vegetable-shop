package com.veggieshop.security;

import com.veggieshop.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurity {

    private final OrderRepository orderRepository;

    /**
     * Checks if the given user is the owner of the order.
     *
     * @param orderId The order ID.
     * @param userId  The user ID (from JWT or authentication).
     * @return true if the user owns the order, false otherwise.
     */
    public boolean isOrderOwner(Long orderId, Long userId) {
        if (orderId == null || userId == null) return false;
        // Exists with both IDs? Safe and performant.
        return orderRepository.existsByIdAndUserId(orderId, userId);
    }
}
