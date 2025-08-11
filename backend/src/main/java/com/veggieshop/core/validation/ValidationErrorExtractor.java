// src/main/java/com/veggieshop/core/validation/ValidationErrorExtractor.java
package com.veggieshop.core.validation;

import jakarta.validation.ConstraintViolation;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.*;

/**
 * Production-grade extractor for Bean Validation errors.
 *
 * Conventions:
 *  - Unified keys per item: { field, message, rejectedValue }
 *  - rejectedValue is sanitized via RejectedValueSanitizer (PII-aware)
 *  - Stable ordering:
 *      * field errors first (by field asc, then message asc),
 *      * then global errors (by message asc)
 *  - Bounded output size via MAX_ERRORS
 *  - Message length bounded via MAX_MESSAGE_LEN
 */
public final class ValidationErrorExtractor {

    /** Max characters for any user-facing message snippet. */
    public static final int MAX_MESSAGE_LEN = 500;

    /** Max number of errors to include. */
    public static final int MAX_ERRORS = 200;

    private ValidationErrorExtractor() {
        // utility
    }

    /** DTO used by both ApiResponse and RFC7807 "errors" array. */
    public record ValidationError(
            @Nullable String field,
            String message,
            @Nullable String rejectedValue
    ) { }

    // =============================================================================================
    // Public API
    // =============================================================================================

    /**
     * Extracts a stable, sanitized list from Spring's BindingResult.
     * Field errors are followed by global errors.
     */
    public static List<ValidationError> fromBindingResult(@Nullable BindingResult br) {
        if (br == null || br.getErrorCount() <= 0) return List.of();

        List<ValidationError> out = new ArrayList<>(Math.min(br.getErrorCount(), MAX_ERRORS));

        // 1) Field errors — stable sort by field asc (nulls last), then message asc
        List<FieldError> fieldErrors = new ArrayList<>(br.getFieldErrors());
        fieldErrors.sort(Comparator
                .comparing((FieldError f) -> nullIfBlank(normalizeField(f.getField())),
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(f -> safeMsg(f.getDefaultMessage()),
                        Comparator.nullsLast(Comparator.naturalOrder()))
        );

        for (FieldError f : fieldErrors) {
            if (out.size() >= MAX_ERRORS) break;
            String field = normalizeField(f.getField());
            String message = safeMsg(f.getDefaultMessage());
            String rejected = RejectedValueSanitizer.sanitize(field, f.getRejectedValue());
            out.add(new ValidationError(field, message, rejected));
        }

        // 2) Global errors — stable sort by message asc
        if (out.size() < MAX_ERRORS && !br.getGlobalErrors().isEmpty()) {
            List<ObjectError> globalErrors = new ArrayList<>(br.getGlobalErrors());
            globalErrors.sort(Comparator.comparing(
                    (ObjectError g) -> safeMsg(g.getDefaultMessage()),
                    Comparator.nullsLast(Comparator.naturalOrder())
            ));
            for (ObjectError g : globalErrors) {
                if (out.size() >= MAX_ERRORS) break;
                out.add(new ValidationError(null, safeMsg(g.getDefaultMessage()), null));
            }
        }

        return List.copyOf(out);
    }

    /**
     * Extracts a stable, sanitized list from ConstraintViolation<?> set.
     */
    public static List<ValidationError> fromConstraintViolations(@Nullable Set<? extends ConstraintViolation<?>> violations) {
        if (violations == null || violations.isEmpty()) return List.of();

        List<ValidationError> out = new ArrayList<>(Math.min(violations.size(), MAX_ERRORS));
        for (ConstraintViolation<?> v : violations) {
            if (out.size() >= MAX_ERRORS) break;
            String field = (v.getPropertyPath() == null) ? null : normalizeField(v.getPropertyPath().toString());
            String message = safeMsg(v.getMessage());
            String rejected = RejectedValueSanitizer.sanitize(field, v.getInvalidValue());
            out.add(new ValidationError(field, message, rejected));
        }

        // Sort: field asc (nulls last), then message asc — to mirror BindingResult behavior
        out.sort(Comparator
                .comparing((ValidationError e) -> nullIfBlank(e.field), Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(e -> e.message, Comparator.nullsLast(Comparator.naturalOrder()))
        );

        return List.copyOf(out);
    }

    /**
     * Concise human message like: "Validation failed (3 errors): email: must be valid".
     * Keeps backward compat for components that still call summarize(..).
     */
    public static String summarize(@Nullable BindingResult br) {
        int total = (br == null) ? 0 : br.getErrorCount();
        String base = "Validation failed" + (total > 0 ? " (" + total + " error" + (total == 1 ? "" : "s") + ")" : "");

        if (br == null || total == 0) return base;

        // Prefer first field error snippet
        if (!br.getFieldErrors().isEmpty()) {
            FieldError f = br.getFieldErrors().get(0);
            String field = normalizeField(f.getField());
            String msg = safeMsg(f.getDefaultMessage());
            String snippet = (field != null) ? (field + ": " + msg) : msg;
            return base + ": " + truncate(snippet, MAX_MESSAGE_LEN);
        }

        // Fallback to first global error
        if (!br.getGlobalErrors().isEmpty()) {
            ObjectError g = br.getGlobalErrors().get(0);
            String snippet = safeMsg(g.getDefaultMessage());
            return base + ": " + truncate(snippet, MAX_MESSAGE_LEN);
        }

        return base;
    }

    // =============================================================================================
    // Internals
    // =============================================================================================

    @Nullable
    private static String normalizeField(@Nullable String field) {
        if (!StringUtils.hasText(field)) return null;
        String f = field.trim();
        // Remove surrounding quotes/backticks if any
        if ((f.startsWith("'") && f.endsWith("'"))
                || (f.startsWith("\"") && f.endsWith("\""))
                || (f.startsWith("`") && f.endsWith("`"))) {
            f = f.substring(1, f.length() - 1);
        }
        return f.isEmpty() ? null : f;
    }

    private static String safeMsg(@Nullable String s) {
        if (!StringUtils.hasText(s)) return "Invalid value";
        return truncate(s.trim(), MAX_MESSAGE_LEN);
    }

    private static String truncate(String s, int max) {
        Objects.requireNonNull(s, "s");
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...";
    }

    @Nullable
    private static String nullIfBlank(@Nullable String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
