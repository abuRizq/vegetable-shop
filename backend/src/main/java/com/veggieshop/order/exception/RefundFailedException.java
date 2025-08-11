package com.veggieshop.order.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a refund attempt cannot be completed successfully.
 * Defaults to HTTP 422 Unprocessable Entity.
 */
public final class RefundFailedException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.UNPROCESSABLE_ENTITY; // 422
    public static final String     DEFAULT_CODE    = "REFUND_FAILED";
    public static final String     DEFAULT_MESSAGE = "Refund processing failed.";

    /**
     * Full constructor preserving all parameters exactly.
     */
    protected RefundFailedException(HttpStatus status,
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

    /** Default constructor (HTTP 422). */
    public RefundFailedException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message (HTTP 422). */
    public RefundFailedException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message + metadata (HTTP 422). */
    public RefundFailedException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message + cause (HTTP 422). */
    public RefundFailedException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Cause-only with default message (HTTP 422). */
    public RefundFailedException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /**
     * Advanced: allow a different HTTP status (e.g., 409 or 502) while keeping same code.
     */
    public RefundFailedException(HttpStatus status, String message, Map<String, Object> metadata) {
        this(status == null ? DEFAULT_STATUS : status, DEFAULT_CODE, message, null, metadata);
    }

    @Override
    public RefundFailedException newInstance(HttpStatus status,
                                             String code,
                                             String message,
                                             Throwable cause,
                                             Map<String, Object> metadata) {
        return new RefundFailedException(status, code, message, cause, metadata);
    }
}
