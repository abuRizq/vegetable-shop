package com.veggieshop.common.dto;

import com.veggieshop.core.validation.ValidationErrorExtractor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;

/**
 * Utility for building standardized 422 UNPROCESSABLE_ENTITY responses.
 */
@Schema(hidden = true)
public final class ApiResponses422 {

    private ApiResponses422() {
    }

    /**
     * Build an ApiResponse for 422 validation failure from a set of ConstraintViolations.
     */
    public static <T> ApiResponse<T> fromViolations(Set<? extends ConstraintViolation<?>> violations) {
        List<ApiResponse.ApiError> errs = ValidationErrorExtractor
                .fromConstraintViolations(violations)
                .stream()
                .map(err -> ApiResponse.ApiError.of(err.field(), err.message(), err.rejectedValue()))
                .toList();

        return ApiResponse.<T>builder(HttpStatus.UNPROCESSABLE_ENTITY, false)
                .code("VALIDATION_FAILED")
                .message("Validation failed")
                .errors(errs.isEmpty() ? null : errs)
                .build();
    }
}
