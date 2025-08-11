package com.veggieshop.core.exception;

import com.veggieshop.core.validation.ValidationErrorExtractor;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Factory for RFC7807 ProblemDetail with 422 UNPROCESSABLE_ENTITY for bean validation failures.
 *
 * NOTE: We build the `errors` extension as List<Map<String,Object>> to avoid generic invariance issues.
 */
public final class ProblemDetails422 {

    private ProblemDetails422() { }

    /**
     * Build a ProblemDetail for 422 validation failure.
     */
    public static ProblemDetail fromViolations(Set<? extends ConstraintViolation<?>> violations) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        pd.setTitle("Validation failed");
        pd.setType(URI.create("https://httpstatuses.io/422"));

        // Build as List<Map<String,Object>> to be safely assignable where List<Object> is expected
        List<Map<String, Object>> errs = ValidationErrorExtractor
                .fromConstraintViolations(violations)
                .stream()
                .map(err -> Map.<String, Object>of(
                        "field", err.field(),
                        "message", err.message(),
                        "rejectedValue", err.rejectedValue()
                ))
                .toList();

        pd.setProperty("errors", errs); // setProperty takes Object; this is fine
        return pd;
    }
}
