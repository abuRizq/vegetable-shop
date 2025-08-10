package com.veggieshop.order;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {

    @Data
    public static class OrderResponse {
        private Long id;
        private Long userId;
        private String userName;
        private BigDecimal totalPrice;
        private String status;
        private LocalDateTime createdAt;
        private List<OrderItemDto.OrderItemResponse> items;
    }

    @Data
    public static class OrderCreateRequest {
        @NotNull
        private List<OrderItemDto.OrderItemCreateRequest> items;
    }
}
