// src/main/java/com/veggieshop/core/exception/ErrorResponseFactory.java
package com.veggieshop.core.exception;

import com.veggieshop.common.dto.ApiResponse;
import com.veggieshop.config.ErrorProps;
import com.veggieshop.core.validation.ValidationErrorExtractor;
import com.veggieshop.core.validation.ValidationPayloads;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Component
public final class ErrorResponseFactory {

    private final ExceptionMappingService mappingService;
    private final ErrorProps errorProps;

    public ErrorResponseFactory(ExceptionMappingService mappingService, ErrorProps errorProps) {
        this.mappingService = Objects.requireNonNull(mappingService, "mappingService");
        this.errorProps = Objects.requireNonNull(errorProps, "errorProps");
    }

    // -------- entry points with unified 'now' --------

    public ResponseEntity<ApiResponse<Void>> fromException(HttpServletRequest request, Throwable ex) {
        return fromException(request, ex, Instant.now());
    }

    public ResponseEntity<ApiResponse<Void>> fromBindingResult(HttpServletRequest request, BindingResult bindingResult) {
        return fromBindingResult(request, bindingResult, Instant.now());
    }

    public ResponseEntity<ApiResponse<Void>> fromException(HttpServletRequest request, Throwable ex, Instant now) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(ex, "ex");
        Objects.requireNonNull(now, "now");

        Throwable root = mappingService.unwrap(ex);
        ExceptionMappingService.Mapping m = mappingService.map(root);

        List<ValidationErrorExtractor.ValidationError> errors = extractValidationErrors(root);

        var shape = ErrorEnvelope.Shape.from(m);
        ApiResponse<Void> body = ErrorEnvelope.toApiResponse(
                shape,
                pathWithQuery(request),
                TraceIdUtil.currentTraceId(),
                errors,
                now
        );
        return ResponseEntity.status(m.status()).body(body);
    }

    public ResponseEntity<ApiResponse<Void>> fromBindingResult(HttpServletRequest request, BindingResult bindingResult, Instant now) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(bindingResult, "bindingResult");
        Objects.requireNonNull(now, "now");

        var bundle = ValidationPayloads.from(bindingResult);

        var shape = new ErrorEnvelope.Shape(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "VALIDATION_FAILED",
                "Validation Failed",
                bundle.summary(),
                java.net.URI.create(errorProps.typeBase() + "validation-failed")
        );

        ApiResponse<Void> body = ErrorEnvelope.toApiResponse(
                shape,
                pathWithQuery(request),
                TraceIdUtil.currentTraceId(),
                bundle.errors(),
                now
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    // -------- helpers --------

    /** Unified extraction so 422 errors appear consistently for both paths. */
    private static List<ValidationErrorExtractor.ValidationError> extractValidationErrors(Throwable ex) {
        if (ex instanceof MethodArgumentNotValidException manve) {
            return ValidationPayloads.from(manve.getBindingResult()).errors();
        }
        if (ex instanceof BindException be) {
            return ValidationPayloads.from(be.getBindingResult()).errors();
        }
        if (ex instanceof ConstraintViolationException cve) {
            return ValidationErrorExtractor.fromConstraintViolations(cve.getConstraintViolations());
        }
        return List.of();
    }

    /** Build request path including query string (keeps parity with ProblemDetails.instance). */
    private static String pathWithQuery(HttpServletRequest req) {
        String uri = (req.getRequestURI() == null) ? "/" : req.getRequestURI();
        String qs = req.getQueryString();
        return (qs == null || qs.isBlank()) ? uri : (uri + "?" + qs);
    }
}
