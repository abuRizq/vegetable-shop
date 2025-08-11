package com.veggieshop.order.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a requested operation cannot proceed until the required payment
 * has been provided or confirmed.
 * Defaults to HTTP 402 Payment Required.
 */
public final class PaymentRequiredException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.PAYMENT_REQUIRED; // 402
    public static final String     DEFAULT_CODE    = "PAYMENT_REQUIRED";
    public static final String     DEFAULT_MESSAGE = "Payment is required to proceed with this operation.";

    /**
     * Full constructor preserving all parameters exactly.
     */
    protected PaymentRequiredException(HttpStatus status,
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
    public PaymentRequiredException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message (HTTP 402). */
    public PaymentRequiredException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message + metadata (HTTP 402). */
    public PaymentRequiredException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message + cause (HTTP 402). */
    public PaymentRequiredException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Cause-only with default message (HTTP 402). */
    public PaymentRequiredException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /**
     * Advanced: allow choosing a different HTTP status (e.g., 409 or 400) while keeping the same code.
     */
    public PaymentRequiredException(HttpStatus status, String message, Map<String, Object> metadata) {
        this(status == null ? DEFAULT_STATUS : status, DEFAULT_CODE, message, null, metadata);
    }

    @Override
    public PaymentRequiredException newInstance(HttpStatus status,
                                                String code,
                                                String message,
                                                Throwable cause,
                                                Map<String, Object> metadata) {
        return new PaymentRequiredException(status, code, message, cause, metadata);
    }
}
