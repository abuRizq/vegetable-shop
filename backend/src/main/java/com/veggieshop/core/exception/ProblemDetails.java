package com.veggieshop.core.exception;

import com.veggieshop.config.ErrorProps;
import com.veggieshop.core.validation.ValidationErrorExtractor;
import com.veggieshop.core.validation.ValidationPayloads;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Production-ready builder for RFC 7807 ProblemDetail responses.
 *
 * Responsibilities:
 *  - Build consistent ProblemDetail from any Throwable using ExceptionMappingService.
 *  - Provide a dedicated 422 builder for BindingResult (@Valid request body) with unified errors[] shape.
 *  - Add common extensions: {code, path, traceId, timestamp, errors[] (when present)}.
 *
 * Notes:
 *  - No logging here; GlobalExceptionHandler owns logging policy.
 *  - The base for "type" URLs is configured via ErrorProps.
 */
@Component
public final class ProblemDetails {

    private static final MediaType PROBLEM_JSON = MediaType.valueOf("application/problem+json");

    private final ExceptionMappingService mappingService;
    private final ErrorProps errorProps;

    public ProblemDetails(ExceptionMappingService mappingService, ErrorProps errorProps) {
        this.mappingService = Objects.requireNonNull(mappingService, "mappingService");
        this.errorProps = Objects.requireNonNull(errorProps, "errorProps");
    }

    // =============================================================================================
    // Entry points (default now = Instant.now())
    // =============================================================================================

    /** Build ResponseEntity<ProblemDetail> from any exception using the centralized mapping. */
    public ResponseEntity<ProblemDetail> fromException(HttpServletRequest request, Throwable ex) {
        return fromException(request, ex, Instant.now());
    }

    /** Build ResponseEntity<ProblemDetail> specifically for BindingResult (@Valid body) with 422. */
    public ResponseEntity<ProblemDetail> fromBindingErrors(HttpServletRequest request, BindingResult br) {
        return fromBindingErrors(request, br, Instant.now());
    }

    /** Convenience to return a ProblemDetail directly (no ResponseEntity). */
    public ProblemDetail of(HttpStatus status, String code, String detail, HttpServletRequest request) {
        return of(status, code, detail, request, Instant.now());
    }

    // =============================================================================================
    // Overloads with unified 'now' (to match ApiResponse timestamps exactly)
    // =============================================================================================

    public ResponseEntity<ProblemDetail> fromException(HttpServletRequest request, Throwable ex, Instant now) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(ex, "ex");
        Objects.requireNonNull(now, "now");

        Throwable root = mappingService.unwrap(ex);
        ExceptionMappingService.Mapping m = mappingService.map(root);

        // Unified extraction: delegate to ValidationErrorExtractor for stable ordering
        List<ValidationErrorExtractor.ValidationError> errors = extractValidationErrors(root);

        ProblemDetail pd = toProblemDetail(
                m.status(),
                m.code(),
                m.message(),
                m.title(),
                m.type(), // shaped by mapping service
                requestPathWithQuery(request),
                TraceIdUtil.currentTraceId(),
                errors,
                now
        );
        return response(m.status(), pd);
    }

    public ResponseEntity<ProblemDetail> fromBindingErrors(HttpServletRequest request, BindingResult br, Instant now) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(br, "bindingResult");
        Objects.requireNonNull(now, "now");

        var bundle = ValidationPayloads.from(br);
        URI type = URI.create(errorProps.typeBase() + "validation-failed");

        ProblemDetail pd = toProblemDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "VALIDATION_FAILED",
                bundle.summary(),                 // detail (human)
                "Validation Failed",              // title (humanized from code)
                type,
                requestPathWithQuery(request),
                TraceIdUtil.currentTraceId(),
                bundle.errors(),
                now
        );
        return response(HttpStatus.UNPROCESSABLE_ENTITY, pd);
    }

    public ProblemDetail of(HttpStatus status, String code, String detail, HttpServletRequest request, Instant now) {
        Objects.requireNonNull(now, "now");
        URI type = URI.create(errorProps.typeBase() + code.trim().toLowerCase().replace('_', '-'));
        return toProblemDetail(
                status,
                code,
                safe(detail, status.getReasonPhrase()),
                humanize(code),
                type,
                requestPathWithQuery(request),
                TraceIdUtil.currentTraceId(),
                null,
                now
        );
    }

    // =============================================================================================
    // Internals
    // =============================================================================================

    private static ProblemDetail toProblemDetail(
            HttpStatus status,
            String code,
            String message,
            String title,
            URI type,
            String path,
            @Nullable String traceId,
            @Nullable List<ValidationErrorExtractor.ValidationError> errors,
            Instant now
    ) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, message);
        pd.setTitle(title);
        pd.setType(type);
        pd.setInstance(URI.create(path));

        pd.setProperty("code", code);
        pd.setProperty("path", path);
        if (traceId != null && !traceId.isBlank()) {
            pd.setProperty("traceId", traceId);
        }
        pd.setProperty("timestamp", now.toString());

        List<Map<String, Object>> errs = ValidationPayloads.toProblemErrors(errors);
        if (!errs.isEmpty()) {
            pd.setProperty("errors", errs);
        }
        return pd;
    }

    private static ResponseEntity<ProblemDetail> response(HttpStatus status, ProblemDetail body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return ResponseEntity.status(status).headers(headers).body(body);
    }

    private static String requestPathWithQuery(HttpServletRequest req) {
        String uri = (req.getRequestURI() == null) ? "/" : req.getRequestURI();
        String qs  = req.getQueryString();
        return (qs == null || qs.isBlank()) ? uri : (uri + "?" + qs);
    }

    private static String humanize(String code) {
        if (code == null || code.isBlank()) return "";
        String t = code.trim().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }

    private static String safe(@Nullable String message, String fallback) {
        if (message == null || message.isBlank()) return fallback;
        String t = message.trim();
        return (t.length() > 500) ? t.substring(0, 500) + "..." : t;
    }

    /** Collect 422 validation errors using the unified extractor for stable ordering. */
    private static List<ValidationErrorExtractor.ValidationError> extractValidationErrors(Throwable ex) {
        if (ex instanceof MethodArgumentNotValidException manve) {
            return ValidationErrorExtractor.fromBindingResult(manve.getBindingResult());
        }
        if (ex instanceof BindException be) {
            return ValidationErrorExtractor.fromBindingResult(be.getBindingResult());
        }
        if (ex instanceof ConstraintViolationException cve) {
            return ValidationErrorExtractor.fromConstraintViolations(cve.getConstraintViolations());
        }
        return List.of();
    }
}
