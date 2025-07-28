package com.veggieshop.service;

import com.veggieshop.dto.OrderDto;

import java.util.List;

public interface OrderService {
    OrderDto.OrderResponse create(Long userId, OrderDto.OrderCreateRequest request);
    OrderDto.OrderResponse findById(Long id);
    List<OrderDto.OrderResponse> findByUser(Long userId);
    List<OrderDto.OrderResponse> findAll();
    void updateStatus(Long orderId, String status);
}
