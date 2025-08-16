package com.veggieshop.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

public class OrderItemDto {

    @Data
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }

    @Data
    public static class OrderItemCreateRequest {
        @NotNull
        private Long productId;
        @NotNull
        private Integer quantity;
    }
}

