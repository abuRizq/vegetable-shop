package com.veggieshop.common;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
final public class ApiResponse<T> {
    private boolean success;
    private T data;
    private Meta meta;
    private ApiError error;

    // ==== Static factory methods ====

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, Meta meta) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .meta(meta)
                .build();
    }

    public static <T> ApiResponse<T> error(ApiError error) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .build();
    }
}
