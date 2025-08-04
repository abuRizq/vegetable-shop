package com.veggieshop.common;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class ApiResponseUtil {

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    public static <T> ResponseEntity<ApiResponse<List<T>>> ok(Page<T> page) {
        Meta meta = Meta.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .sort(
                        page.getSort().stream()
                                .map(order -> order.getProperty() + "," + order.getDirection())
                                .collect(Collectors.toList())
                )
                .build();
        return ResponseEntity.ok(ApiResponse.success(page.getContent(), meta));
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, Meta meta) {
        return ResponseEntity.ok(ApiResponse.success(data, meta));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data));
    }

    public static ResponseEntity<ApiResponse<Object>> error(HttpStatus status, String message, String path) {
        ApiError apiError = ApiError.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(status).body(ApiResponse.error(apiError));
    }

    public static ResponseEntity<ApiResponse<Object>> error(ApiError apiError, HttpStatus status) {
        return ResponseEntity.status(status).body(ApiResponse.error(apiError));
    }

    public static <T> ResponseEntity<ApiResponse<T>> noContent() {
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.<T>builder()
                        .success(true)
                        .data(null)
                        .meta(null)
                        .error(null)
                        .build());
    }

}
