package com.veggieshop.core.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bridges validation results into a reusable bundle + helpers.
 * IMPORTANT: Reuses ValidationErrorExtractor.ValidationError so we avoid type duplication.
 *
 * Updated:
 *  - ConstraintViolationException path now delegates to ValidationErrorExtractor
 *    for stable ordering and normalization across all 422 paths.
 */
public final class ValidationPayloads {

    private ValidationPayloads() { }

    /** Canonical bundle for 422 responses: list of errors + a short human summary. */
    public record ValidationBundle(
            List<ValidationErrorExtractor.ValidationError> errors,
            String summary
    ) {}

    /** Build a bundle from a BindingResult (@Valid request body / binding). */
    public static ValidationBundle from(BindingResult br) {
        List<ValidationErrorExtractor.ValidationError> list = ValidationErrorExtractor.fromBindingResult(br);
        String summary = ValidationErrorExtractor.summarize(br);
        return new ValidationBundle(list, summary);
    }

    /** Build a bundle from a ConstraintViolationException (@Validated method parameters, etc.). */
    public static ValidationBundle from(ConstraintViolationException cve) {
        List<ValidationErrorExtractor.ValidationError> list =
                ValidationErrorExtractor.fromConstraintViolations(cve.getConstraintViolations());
        String summary = "Validation failed (" + list.size() + " error" + (list.size() == 1 ? "" : "s") + ")";
        return new ValidationBundle(list, summary);
    }

    /**
     * Convert our error list into RFC7807 `errors` extension (List<Map<String,Object>>).
     * Using Object values keeps this shape future-proof if we later include non-String values.
     */
    public static List<Map<String, Object>> toProblemErrors(
            @Nullable List<ValidationErrorExtractor.ValidationError> list) {
        if (list == null || list.isEmpty()) return List.of();
        return list.stream()
                .map(e -> Map.<String, Object>of(
                        "field", e.field(),
                        "message", e.message(),
                        "rejectedValue", e.rejectedValue()
                ))
                .toList();
    }
}
