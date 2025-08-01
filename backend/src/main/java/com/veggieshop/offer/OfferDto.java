package com.veggieshop.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class OfferDto {

    @Data
    public static class OfferResponse {
        private Long id;
        private Long productId;
        private String productName;
        private BigDecimal discount;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Data
    public static class OfferCreateRequest {
        @NotNull
        private Long productId;
        @NotNull
        private BigDecimal discount;
        @NotNull
        private LocalDate startDate;
        @NotNull
        private LocalDate endDate;
    }
}
