package com.veggieshop.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProductDto {

    @Data
    public static class ProductResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private BigDecimal discount;
        private boolean featured;
        private Long soldCount;
        private String imageUrl;
        private Long categoryId;
        private String categoryName;
    }

    @Data
    public static class ProductCreateRequest {
        @NotBlank
        private String name;

        private String description;

        @NotNull
        private BigDecimal price;

        private BigDecimal discount;

        private boolean featured;

        @NotNull
        private Long categoryId;

        private String imageUrl;
    }

    @Data
    public static class ProductUpdateRequest {
        @NotBlank
        private String name;

        private String description;

        @NotNull
        private BigDecimal price;

        private BigDecimal discount;

        private boolean featured;

        @NotNull
        private Long categoryId;

        private String imageUrl;
    }
}
