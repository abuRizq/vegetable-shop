package com.veggieshop.pricing.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when the system fails to find a price for a given product,
 * SKU, or pricing context.
 * Defaults to HTTP 404 Not Found.
 */
public final class PriceNotFoundException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.NOT_FOUND; // 404
    public static final String     DEFAULT_CODE    = "PRICE_NOT_FOUND";
    public static final String     DEFAULT_MESSAGE = "Price information not found for the requested product.";

    /**
     * Full-argument constructor (preserves all parameters exactly).
     */
    protected PriceNotFoundException(HttpStatus status,
                                     String code,
                                     String message,
                                     Throwable cause,
                                     Map<String, Object> metadata) {
        super(status,
                (code == null || code.isBlank()) ? DEFAULT_CODE : code,
                (message == null || message.isBlank()) ? DEFAULT_MESSAGE : message,
                cause,
                metadata);
    }

    /** Default constructor (HTTP 404). */
    public PriceNotFoundException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message (HTTP 404). */
    public PriceNotFoundException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message + metadata (HTTP 404). */
    public PriceNotFoundException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message + cause (HTTP 404). */
    public PriceNotFoundException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Cause-only with default message (HTTP 404). */
    public PriceNotFoundException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /** Allow custom HTTP status while keeping same code. */
    public PriceNotFoundException(HttpStatus status, String message, Map<String, Object> metadata) {
        this(status == null ? DEFAULT_STATUS : status, DEFAULT_CODE, message, null, metadata);
    }

    @Override
    public PriceNotFoundException newInstance(HttpStatus status,
                                              String code,
                                              String message,
                                              Throwable cause,
                                              Map<String, Object> metadata) {
        return new PriceNotFoundException(status, code, message, cause, metadata);
    }
}
