package com.veggieshop.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDto.OrderResponse create(Long userId, OrderDto.OrderCreateRequest request);
    OrderDto.OrderResponse findById(Long id);

    Page<OrderDto.OrderResponse> findByUser(Long userId, Pageable pageable);

    Page<OrderDto.OrderResponse> findAll(Pageable pageable);

    // Optional: find by status
    Page<OrderDto.OrderResponse> findByStatus(String status, Pageable pageable);

    // Optional: by user and status
    Page<OrderDto.OrderResponse> findByUserAndStatus(Long userId, String status, Pageable pageable);

    void updateStatus(Long orderId, String status);
}
