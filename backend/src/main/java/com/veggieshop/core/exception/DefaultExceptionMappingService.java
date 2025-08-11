package com.veggieshop.core.exception;

import com.veggieshop.config.ErrorProps;
import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Production-ready implementation of ExceptionMappingService.
 * - Maps Throwable -> (status, code, message) with stable, normalized codes.
 * - Title derived from code (humanized) and type from ShapeStrategy (configured base).
 * - Bounded unwrap policy; no logging here (handlers own logging).
 */
public final class DefaultExceptionMappingService implements ExceptionMappingService {

    public static final int DEFAULT_MAX_UNWRAP_DEPTH = 5;

    private final int maxUnwrapDepth;
    private final ShapeStrategy shape;

    /**
     * Preferred constructor – injects ErrorProps to source the typeBase.
     */
    public DefaultExceptionMappingService(ErrorProps errorProps) {
        this(DEFAULT_MAX_UNWRAP_DEPTH, new DefaultShapeStrategy(
                errorProps == null ? null : errorProps.typeBase()
        ));
    }

    /**
     * Backwards-compatible constructor (kept for tests/manual wiring).
     * Pass a ShapeStrategy that already knows the base URL.
     */
    public DefaultExceptionMappingService(int maxUnwrapDepth, ShapeStrategy shape) {
        this.maxUnwrapDepth = (maxUnwrapDepth <= 0) ? DEFAULT_MAX_UNWRAP_DEPTH : maxUnwrapDepth;
        this.shape = Objects.requireNonNull(shape, "shape");
    }

    // =============================================================================================
    // ExceptionMappingService
    // =============================================================================================

    @Override
    public Mapping map(Throwable ex) {
        Objects.requireNonNull(ex, "ex");

        // Respect ResponseStatusException (status + reason/detail)
        if (ex instanceof ResponseStatusException rse) {
            HttpStatus st = HttpStatus.valueOf(rse.getStatusCode().value());
            String code = ensureCode(st.name(), st.name()); // keep stable fallback
            String msg  = safeMessage(rse.getReason(), defaultReason(st));
            return shaped(st, code, msg);
        }

        // Respect ErrorResponseException (status + ProblemDetail if present)
        if (ex instanceof ErrorResponseException ere) {
            HttpStatus st = HttpStatus.valueOf(ere.getStatusCode().value());
            ProblemDetail pd = ere.getBody();
            String detail = (pd != null && StringUtils.hasText(pd.getDetail())) ? pd.getDetail() : ere.getMessage();
            String msg = safeMessage(detail, defaultReason(st));
            String code = ensureCode(st.name(), st.name());
            return shaped(st, code, msg);
        }

        // Respect @ResponseStatus on exception classes
        ResponseStatus rs = ex.getClass().getAnnotation(ResponseStatus.class);
        if (rs != null) {
            HttpStatus st = rs.code();
            String msg = StringUtils.hasText(rs.reason()) ? rs.reason() : defaultReason(st);
            return shaped(st, ensureCode(st.name(), st.name()), safeMessage(msg, defaultReason(st)));
        }

        // --- 400 family ---
        if (ex instanceof MissingServletRequestParameterException e) {
            return shaped(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER",
                    "Missing required parameter: " + e.getParameterName());
        }
        if (ex instanceof MissingRequestHeaderException e) {
            return shaped(HttpStatus.BAD_REQUEST, "MISSING_HEADER",
                    "Missing required header: " + e.getHeaderName());
        }
        if (ex instanceof MethodArgumentTypeMismatchException e) {
            return shaped(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH",
                    "Parameter '" + e.getName() + "' has invalid value");
        }
        if (ex instanceof org.springframework.http.converter.HttpMessageNotReadableException) {
            return shaped(HttpStatus.BAD_REQUEST, "MALFORMED_JSON", "Request body is malformed or unreadable");
        }

        // --- 401 / 403 ---
        if (ex instanceof AuthenticationException) {
            return shaped(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication is required");
        }
        if (ex instanceof AccessDeniedException) {
            return shaped(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have permission to access this resource");
        }

        // --- 404 ---
        if (ex instanceof NoSuchElementException || isClassOrInterfaceOnHierarchy(ex, "EntityNotFoundException")) {
            return shaped(HttpStatus.NOT_FOUND, "NOT_FOUND", "The requested resource was not found");
        }
        if (ex instanceof NoHandlerFoundException) {
            return shaped(HttpStatus.NOT_FOUND, "NO_HANDLER", "No handler found for the requested path");
        }

        // --- 405 / 415 ---
        if (ex instanceof HttpRequestMethodNotSupportedException) {
            return shaped(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "HTTP method not allowed for this endpoint");
        }
        if (ex instanceof HttpMediaTypeNotSupportedException) {
            return shaped(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", "Unsupported media type");
        }

        // --- 409 family ---
        if (ex instanceof org.springframework.dao.DuplicateKeyException) {
            return shaped(HttpStatus.CONFLICT, "DUPLICATE_KEY", "A record with the same key already exists");
        }
        if (ex instanceof org.springframework.dao.DataIntegrityViolationException) {
            return shaped(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION", "Request conflicts with data integrity constraints");
        }
        if (ex instanceof org.springframework.dao.OptimisticLockingFailureException) {
            return shaped(HttpStatus.CONFLICT, "OPTIMISTIC_LOCK_FAILURE", "The resource was modified by another transaction");
        }

        // --- 422 family ---
        if (ex instanceof MethodArgumentNotValidException
                || ex instanceof BindException
                || ex instanceof ConstraintViolationException) {
            return shaped(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_FAILED", "Validation failed");
        }

        // --- 429 ---
        if (isClassOrInterfaceOnHierarchy(ex, "RateLimitExceededException")) {
            return shaped(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", "Too many requests. Please try again later");
        }

        // --- 503 ---
        if (isClassOrInterfaceOnHierarchy(ex, "ServiceUnavailableException")) {
            return shaped(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE",
                    safeMessage(ex.getMessage(), "Service is temporarily unavailable"));
        }

        // --- ApplicationException (first-class) ---
        if (ex instanceof ApplicationException ae) {
            HttpStatus st = (ae.getHttpStatus() != null) ? ae.getHttpStatus() : HttpStatus.BAD_REQUEST;
            String code = ensureCode(ae.getCode(), "APPLICATION_ERROR");
            String msg  = safeMessage(ae.getMessage(), "Application error");
            return shaped(st, code, msg);
        }

        // --- ApplicationException-like via reflection fallback ---
        if (isClassOrInterfaceOnHierarchy(ex, "ApplicationException")) {
            Mapping m = reflectApplicationException(ex);
            if (m != null) return m;
            return shaped(HttpStatus.BAD_REQUEST, "APPLICATION_ERROR",
                    safeMessage(ex.getMessage(), "Application error"));
        }

        // --- default 500 ---
        return shaped(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred");
    }

    @Override
    public Throwable unwrap(@Nullable Throwable t) {
        if (t == null) return new RuntimeException("Unknown error");
        Throwable cur = t;
        int depth = 0, max = Math.max(1, this.maxUnwrapDepth);
        while (cur.getCause() != null && depth < max) {
            // Unwrap common wrappers only; keep semantics of checked exceptions
            if (cur instanceof RuntimeException || cur instanceof ServletException) {
                cur = cur.getCause();
                depth++;
            } else {
                break;
            }
        }
        return cur;
    }

    // =============================================================================================
    // Shape strategy (title/type)
    // =============================================================================================

    public interface ShapeStrategy {
        String titleFor(String code, HttpStatus status);
        URI typeFor(String code);
    }

    /** Default: title from code (humanized), type from configured base + code-kebab. */
    public static final class DefaultShapeStrategy implements ShapeStrategy {
        private final String base;

        public DefaultShapeStrategy(String base) {
            String b = (base == null || base.isBlank())
                    ? "https://docs.veggieshop.example/errors/"
                    : base;
            this.base = b.endsWith("/") ? b : b + "/";
        }

        @Override
        public String titleFor(String code, HttpStatus status) {
            if (!StringUtils.hasText(code)) return status.getReasonPhrase();
            String t = code.trim().toLowerCase(Locale.ROOT).replace('_', ' ');
            return StringUtils.capitalize(t);
        }

        @Override
        public URI typeFor(String code) {
            String c = (StringUtils.hasText(code) ? code : "unknown")
                    .trim().toLowerCase(Locale.ROOT).replace('_', '-');
            return URI.create(base + c);
        }
    }

    // =============================================================================================
    // Internals
    // =============================================================================================

    private Mapping shaped(HttpStatus status, String code, String message) {
        String finalCode = ensureCode(code, status.name());
        String title = shape.titleFor(finalCode, status);
        URI type = shape.typeFor(finalCode);
        return new Mapping(status, finalCode, message, title, type);
    }

    @Nullable
    private Mapping reflectApplicationException(Throwable ex) {
        try {
            Method getCode = safeMethod(ex.getClass(), "getCode");
            Method getHttpStatus = safeMethod(ex.getClass(), "getHttpStatus");
            if (getHttpStatus == null) getHttpStatus = safeMethod(ex.getClass(), "getStatus");
            Method getMessage = safeMethod(ex.getClass(), "getMessage");

            String code = (getCode != null) ? Objects.toString(getCode.invoke(ex), null) : null;
            Object statusObj = (getHttpStatus != null) ? getHttpStatus.invoke(ex) : HttpStatus.BAD_REQUEST;
            HttpStatus st = coerceStatus(statusObj, HttpStatus.BAD_REQUEST);
            String msg = (getMessage != null) ? Objects.toString(getMessage.invoke(ex), null) : null;

            code = ensureCode(code, "APPLICATION_ERROR");
            msg  = safeMessage(msg, "Application error");
            return shaped(st, code, msg);
        } catch (Throwable ignore) {
            return null;
        }
    }

    private static Method safeMethod(Class<?> t, String name) {
        try { return t.getMethod(name); }
        catch (NoSuchMethodException e) { return null; }
    }

    private static HttpStatus coerceStatus(@Nullable Object o, HttpStatus fallback) {
        if (o instanceof HttpStatus hs) return hs;
        if (o instanceof Number n) return HttpStatus.valueOf(n.intValue());
        return fallback;
    }

    /**
     * Walks the class hierarchy (including interfaces) and checks by simple name.
     * Useful when we cannot depend on a concrete class at compile time (e.g., proxies or optional libs).
     */
    private static boolean isClassOrInterfaceOnHierarchy(Throwable ex, String simpleName) {
        Class<?> c = ex.getClass();
        while (c != null) {
            if (c.getSimpleName().equals(simpleName)) return true;
            for (Class<?> itf : c.getInterfaces()) {
                if (itf.getSimpleName().equals(simpleName)) return true;
            }
            c = c.getSuperclass();
        }
        return false;
    }

    private static String defaultReason(HttpStatus status) {
        return status.getReasonPhrase();
    }

    /** Trims, normalizes, caps messages; appends truncation hint for likely JSON bodies. */
    private static String safeMessage(@Nullable String message, String fallback) {
        if (!StringUtils.hasText(message)) return fallback;
        String t = message.trim();
        final int MAX = 500;
        if (t.length() <= MAX) return t;

        boolean looksJson = (t.startsWith("{") && t.endsWith("}")) || (t.startsWith("[") && t.endsWith("]"));
        String head = t.substring(0, Math.min(MAX, t.length()));
        return looksJson ? (head + "...(truncated)") : (head + "...");
    }

    /** Ensures a stable, machine-friendly code. */
    private static String ensureCode(@Nullable String s, String fallback) {
        String v = (s == null || s.isBlank()) ? fallback : s.trim();
        return v.replace(' ', '_').toUpperCase(Locale.ROOT);
    }
}
