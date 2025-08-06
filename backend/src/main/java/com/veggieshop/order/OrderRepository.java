package com.veggieshop.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Paged retrieval of all orders
    Page<Order> findAll(Pageable pageable);

    // Paged retrieval of orders by user
    Page<Order> findByUserId(Long userId, Pageable pageable);

    // Optionally filter by status
    Page<Order> findByStatus(Order.Status status, Pageable pageable);

    // Filter by user and status
    Page<Order> findByUserIdAndStatus(Long userId, Order.Status status, Pageable pageable);

    boolean existsByIdAndUserId(Long orderId, Long userId);
}
