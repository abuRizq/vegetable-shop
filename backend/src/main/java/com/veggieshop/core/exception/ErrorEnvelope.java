package com.veggieshop.core.exception;

import com.veggieshop.common.dto.ApiResponse;
import com.veggieshop.core.validation.ValidationErrorExtractor;
import com.veggieshop.core.validation.ValidationPayloads;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.Nullable;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Central adapter to convert a mapped error "shape" to:
 *  - RFC 7807 ProblemDetail, or
 *  - The unified ApiResponse error envelope.
 *
 * Keeps shared fields (code, path, traceId, timestamp, errors[]) consistent.
 */
final class ErrorEnvelope {

    private ErrorEnvelope() { }

    /**
     * Minimal, immutable shape produced by ExceptionMappingService.
     */
    record Shape(HttpStatus status,
                 String code,
                 String title,
                 String message,
                 URI type) {
        public Shape {
            Objects.requireNonNull(status, "status");
            Objects.requireNonNull(code, "code");
            Objects.requireNonNull(title, "title");
            Objects.requireNonNull(message, "message");
            Objects.requireNonNull(type, "type");
        }

        public static Shape from(ExceptionMappingService.Mapping m) {
            return new Shape(m.status(), m.code(), m.title(), m.message(), m.type());
        }
    }

    // ---------------------------------------------------------------------------------------------
    // ProblemDetail builder
    // ---------------------------------------------------------------------------------------------

    /**
     * Build a ProblemDetail from a common shape.
     * Uses Instant.now() for timestamp; see overload to pass a unified Instant.
     */
    public static ProblemDetail toProblemDetail(Shape s,
                                                HttpServletRequest req,
                                                @Nullable String traceId,
                                                @Nullable List<ValidationErrorExtractor.ValidationError> errors) {
        return toProblemDetail(s, req, traceId, errors, Instant.now());
    }

    /**
     * Build a ProblemDetail from a common shape with a supplied 'now' for timestamp unification.
     */
    public static ProblemDetail toProblemDetail(Shape s,
                                                HttpServletRequest req,
                                                @Nullable String traceId,
                                                @Nullable List<ValidationErrorExtractor.ValidationError> errors,
                                                Instant now) {
        Objects.requireNonNull(s, "shape");
        Objects.requireNonNull(req, "request");
        Objects.requireNonNull(now, "now");

        String instance = requestPathWithQuery(req);

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(s.status(), s.message());
        pd.setTitle(s.title());
        pd.setType(s.type());
        pd.setInstance(URI.create(instance));

        pd.setProperty("code", s.code());
        pd.setProperty("path", instance);
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

    /** Convenience overload using a ValidationBundle. */
    public static ProblemDetail toProblemDetail(Shape s,
                                                HttpServletRequest req,
                                                @Nullable String traceId,
                                                ValidationPayloads.ValidationBundle bundle) {
        return toProblemDetail(s, req, traceId,
                bundle == null ? null : bundle.errors(),
                Instant.now());
    }

    /** Convenience overload using a ValidationBundle and a unified Instant. */
    public static ProblemDetail toProblemDetail(Shape s,
                                                HttpServletRequest req,
                                                @Nullable String traceId,
                                                ValidationPayloads.ValidationBundle bundle,
                                                Instant now) {
        return toProblemDetail(s, req, traceId,
                bundle == null ? null : bundle.errors(),
                now);
    }

    // ---------------------------------------------------------------------------------------------
    // ApiResponse builder
    // ---------------------------------------------------------------------------------------------

    /**
     * Build an ApiResponse<Void> from a common shape.
     * Uses Instant.now() unless you call the overload with a supplied 'now'.
     */
    public static ApiResponse<Void> toApiResponse(Shape s,
                                                  @Nullable String path,
                                                  @Nullable String traceId,
                                                  @Nullable List<ValidationErrorExtractor.ValidationError> errors) {
        return toApiResponse(s, path, traceId, errors, Instant.now());
    }

    /**
     * Build an ApiResponse<Void> from a common shape with a supplied 'now' for timestamp unification.
     */
    public static ApiResponse<Void> toApiResponse(Shape s,
                                                  @Nullable String path,
                                                  @Nullable String traceId,
                                                  @Nullable List<ValidationErrorExtractor.ValidationError> errors,
                                                  Instant now) {
        Objects.requireNonNull(s, "shape");
        Objects.requireNonNull(now, "now");

        List<ApiResponse.ApiError> apiErrors = (errors == null || errors.isEmpty())
                ? null
                : errors.stream()
                .map(e -> ApiResponse.ApiError.of(e.field(), e.message(), e.rejectedValue()))
                .toList();

        return ApiResponse.<Void>builder(s.status(), false)
                .code(s.code())
                .message(s.message())
                .errors(apiErrors)
                .path(safePath(path))
                .traceId(isBlank(traceId) ? null : traceId)
                .timestamp(now) // unify timestamp with ProblemDetail if caller passes the same 'now'
                .build();
    }

    /** Convenience overload using a ValidationBundle. */
    public static ApiResponse<Void> toApiResponse(Shape s,
                                                  @Nullable String path,
                                                  @Nullable String traceId,
                                                  ValidationPayloads.ValidationBundle bundle) {
        return toApiResponse(s, path, traceId,
                bundle == null ? null : bundle.errors(),
                Instant.now());
    }

    /** Convenience overload using a ValidationBundle and a unified Instant. */
    public static ApiResponse<Void> toApiResponse(Shape s,
                                                  @Nullable String path,
                                                  @Nullable String traceId,
                                                  ValidationPayloads.ValidationBundle bundle,
                                                  Instant now) {
        return toApiResponse(s, path, traceId,
                bundle == null ? null : bundle.errors(),
                now);
    }

    // ---------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------

    private static String requestPathWithQuery(HttpServletRequest req) {
        String uri = req.getRequestURI() == null ? "/" : req.getRequestURI();
        String qs = req.getQueryString();
        return (qs == null || qs.isBlank()) ? uri : (uri + "?" + qs);
    }

    private static String safePath(@Nullable String p) {
        return (p == null || p.isBlank()) ? "/" : p;
    }

    private static boolean isBlank(@Nullable String s) {
        return s == null || s.isBlank();
    }

    // Optional mutable map builder (kept for future extension)
    @SuppressWarnings("unused")
    private static Map<String, Object> mapOf(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return m;
    }
}
