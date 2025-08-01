package com.veggieshop.order;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    // Check if there are any order items for a given product
    boolean existsByProductId(Long productId);
}
