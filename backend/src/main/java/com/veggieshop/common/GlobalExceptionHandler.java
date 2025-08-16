package com.veggieshop.common;

import com.veggieshop.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final Environment environment;

    public GlobalExceptionHandler(Environment environment) {
        this.environment = environment;
    }

    // === 404 - Resource not found (custom) ===
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI());
    }

    // === 400 - Bad request (custom) ===
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI());
    }

    // === 409 - Duplicate resource (custom) ===
    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicate(DuplicateException ex, HttpServletRequest req) {
        return errorResponse(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
    }

    // === 400 - Invalid or expired reset token (custom) ===
    @ExceptionHandler(InvalidResetTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidResetToken(InvalidResetTokenException ex, HttpServletRequest req) {
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI());
    }

    // === 403 - Access denied (Spring Security) ===
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return errorResponse(HttpStatus.FORBIDDEN, "You are not authorized to access this resource.", req.getRequestURI());
    }

    // === 401 - Unauthorized (custom, e.g. JWT expired) ===
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorized(UnauthorizedException ex, HttpServletRequest req) {
        return errorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), req.getRequestURI());
    }

    // === 401 - Bad credentials (Spring Security) ===
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        return errorResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials provided.", req.getRequestURI());
    }

    // === 400 - Missing required cookie ===
    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingCookie(MissingRequestCookieException ex, HttpServletRequest req) {
        String message = "Missing cookie: " + ex.getCookieName();
        return errorResponse(HttpStatus.BAD_REQUEST, message, req.getRequestURI());
    }

    // === 400 - Validation errors (@Valid annotated DTOs) ===
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (msg1, msg2) -> msg1 // ignore duplicates
                ));
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message("Validation failed for some fields.")
                .path(req.getRequestURI())
                .timestamp(Instant.now())
                .fieldErrors(errors)
                .build();
        return ResponseEntity.badRequest().body(ApiResponse.error(apiError));
    }

    // === 400 - Constraint violations (method param validation) ===
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex, HttpServletRequest req) {

        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> {
                            String path = v.getPropertyPath().toString();
                            int dot = path.lastIndexOf('.');
                            return dot >= 0 ? path.substring(dot + 1) : path;
                        },
                        jakarta.validation.ConstraintViolation::getMessage,
                        (m1, m2) -> m1
                ));

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message("Validation failed for some parameters")
                .path(req.getRequestURI())
                .timestamp(Instant.now())
                .fieldErrors(errors)
                .build();
        return ResponseEntity.badRequest().body(ApiResponse.error(apiError));
    }

    // === 400 - Illegal argument (Java built-in) ===
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI());
    }

    // === 404 - No handler found (invalid endpoint) ===
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandler(NoHandlerFoundException ex, HttpServletRequest req) {
        String message = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();
        return errorResponse(HttpStatus.NOT_FOUND, message, req.getRequestURI());
    }

    // === 404 - No resource found (static or REST resources) ===
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest req) {
        String message = "No resource found for " + ex.getHttpMethod() + " " + ex.getResourcePath();
        return errorResponse(HttpStatus.NOT_FOUND, message, req.getRequestURI());
    }

    // === 409 - Database integrity violation ===
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest req) {
        log.error("Data integrity violation at {}: {}", req.getRequestURI(), ex.getMessage());
        String message = "A database integrity constraint was violated.";
        if (isDevProfile() && ex.getMostSpecificCause() != null) {
            message += " Cause: " + ex.getMostSpecificCause().getMessage();
        }
        return errorResponse(HttpStatus.CONFLICT, message, req.getRequestURI());
    }

    // === 500 - Fallback for any unhandled exception ===
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAll(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception at {}: ", req.getRequestURI(), ex);
        String message = "An unexpected error occurred.";
        if (isDevProfile()) {
            message += " [" + ex.getClass().getSimpleName() + "] " + ex.getMessage();
        }
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, req.getRequestURI());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Object>> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatusCode statusCode = ex.getStatusCode();
        String reasonPhrase = (statusCode instanceof HttpStatus)
                ? ((HttpStatus) statusCode).getReasonPhrase()
                : statusCode.toString();

        ApiError apiError = ApiError.builder()
                .status(statusCode.value())
                .error(reasonPhrase)
                .message(ex.getReason())
                .path(req.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(statusCode).body(ApiResponse.error(apiError));
    }


    // ==== Helper: Build unified error responses ====
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

    // ==== Helper: Check if 'dev' profile is active ====
    private boolean isDevProfile() {
        String[] profiles = environment.getActiveProfiles();
        for (String profile : profiles) {
            if ("dev".equalsIgnoreCase(profile)) return true;
        }
        return false;
    }
}
