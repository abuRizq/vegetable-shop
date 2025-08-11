package com.veggieshop.core.exception;

import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.Objects;

/**
 * Contract for mapping any Throwable to a stable, documented HTTP error shape.
 *
 * Responsibilities:
 *  - Provide a stable (status, code, message) triple for clients.
 *  - Derive a human-friendly title and a documentation type URI from the code.
 *  - Offer a safe unwrap policy for common wrapper exceptions.
 *
 * Notes:
 *  - Implementations must NEVER return null from map(..).
 *  - Code MUST be non-blank (throw IllegalArgumentException if violated).
 *  - No logging here; handlers own logging responsibilities.
 */
public interface ExceptionMappingService {

    /**
     * Immutable mapping result used by ProblemDetails / ErrorResponseFactory.
     */
    record Mapping(HttpStatus status,
                   String code,
                   String message,
                   String title,
                   URI type) {
        public Mapping {
            Objects.requireNonNull(status, "status");
            Objects.requireNonNull(message, "message");
            Objects.requireNonNull(title, "title");
            Objects.requireNonNull(type, "type");
            if (code == null || code.isBlank()) {
                throw new IllegalArgumentException("code must not be blank");
            }
        }
    }

    /**
     * Map a Throwable into a stable HTTP error shape.
     * Must never return null.
     */
    Mapping map(Throwable ex);

    /**
     * Safely unwrap common wrapper exceptions with a bounded depth.
     * Must never return null (return the original if no unwrap occurs).
     */
    Throwable unwrap(Throwable ex);
}
