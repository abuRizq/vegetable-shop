package com.veggieshop.common;

import com.veggieshop.exception.BadRequestException;
import com.veggieshop.exception.DuplicateException;
import com.veggieshop.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI());
    }

    // Bad request
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI());
    }

    // Duplicate
    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicate(DuplicateException ex, HttpServletRequest req) {
        return errorResponse(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
    }

    // Access denied (Spring Security)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return errorResponse(HttpStatus.FORBIDDEN, "You are not authorized to access this resource.", req.getRequestURI());
    }

    // Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (m1, m2) -> m1
                ));
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message("Validation failed for some fields")
                .path(req.getRequestURI())
                .timestamp(Instant.now())
                .fieldErrors(errors) // << هنا
                .build();
        return ResponseEntity.badRequest().body(ApiResponse.error(apiError));
    }

    // Fallback (Any other exception)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAll(Exception ex, HttpServletRequest req) {
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", req.getRequestURI());
    }

    // Helper method
    private ResponseEntity<ApiResponse<Object>> errorResponse(HttpStatus status, String message, String path) {
        ApiError apiError = ApiError.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(status).body(ApiResponse.error(apiError));
    }
}
