package com.veggieshop.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

public class CategoryDto {

    @Data
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String description;
    }

    @Data
    public static class CategoryCreateRequest {
        @NotBlank
        private String name;
        private String description;
    }

    @Data
    public static class CategoryUpdateRequest {
        @NotBlank
        private String name;
        private String description;
    }
}
