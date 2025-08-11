// src/main/java/com/veggieshop/core/exception/ErrorShape.java
package com.veggieshop.core.exception;

/**
 * Legacy constants for error "type" URLs in RFC 7807 payloads.
 *
 * <p>Deprecated: prefer {@link com.veggieshop.config.ErrorProps#typeBase()}
 * so the base URL can be configured per environment.</p>
 */
@Deprecated(forRemoval = true, since = "2.0.0")
public final class ErrorShape {

    /**
     * Legacy default base for ProblemDetail.type URIs.
     * Example: https://docs.veggieshop.example/errors/validation-failed
     */
    public static final String TYPE_BASE = "https://docs.veggieshop.example/errors/";

    private ErrorShape() {
        // utility class
    }
}
