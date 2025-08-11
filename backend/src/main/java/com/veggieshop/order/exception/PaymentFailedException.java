package com.veggieshop.order.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a payment attempt fails during order processing.
 * Defaults to HTTP 402 Payment Required.
 */
public final class PaymentFailedException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.PAYMENT_REQUIRED; // 402
    public static final String     DEFAULT_CODE    = "PAYMENT_FAILED";
    public static final String     DEFAULT_MESSAGE = "Payment processing failed.";

    /**
     * Full constructor preserving all parameters exactly.
     */
    protected PaymentFailedException(HttpStatus status,
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

    /** Default constructor (HTTP 402). */
    public PaymentFailedException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message (HTTP 402). */
    public PaymentFailedException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message + metadata (HTTP 402). */
    public PaymentFailedException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message + cause (HTTP 402). */
    public PaymentFailedException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Cause-only with default message (HTTP 402). */
    public PaymentFailedException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /**
     * Advanced: allow a different HTTP status (e.g., 400, 409, 502) while keeping same code.
     */
    public PaymentFailedException(HttpStatus status, String message, Map<String, Object> metadata) {
        this(status == null ? DEFAULT_STATUS : status, DEFAULT_CODE, message, null, metadata);
    }

    @Override
    public PaymentFailedException newInstance(HttpStatus status,
                                              String code,
                                              String message,
                                              Throwable cause,
                                              Map<String, Object> metadata) {
        return new PaymentFailedException(status, code, message, cause, metadata);
    }
}
