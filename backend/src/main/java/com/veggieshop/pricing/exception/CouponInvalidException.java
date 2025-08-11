package com.veggieshop.pricing.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when the provided coupon code is invalid or cannot be applied.
 * Defaults to HTTP 422 Unprocessable Entity.
 */
public final class CouponInvalidException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.UNPROCESSABLE_ENTITY; // 422
    public static final String     DEFAULT_CODE    = "COUPON_INVALID";
    public static final String     DEFAULT_MESSAGE = "The provided coupon code is invalid or cannot be applied.";

    /**
     * Full-argument constructor (preserves all parameters exactly).
     */
    protected CouponInvalidException(HttpStatus status,
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
    public CouponInvalidException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message (HTTP 422). */
    public CouponInvalidException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message + metadata (HTTP 422). */
    public CouponInvalidException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message + cause (HTTP 422). */
    public CouponInvalidException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Cause-only with default message (HTTP 422). */
    public CouponInvalidException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /** Allow custom HTTP status while keeping same code. */
    public CouponInvalidException(HttpStatus status, String message, Map<String, Object> metadata) {
        this(status == null ? DEFAULT_STATUS : status, DEFAULT_CODE, message, null, metadata);
    }

    @Override
    public CouponInvalidException newInstance(HttpStatus status,
                                              String code,
                                              String message,
                                              Throwable cause,
                                              Map<String, Object> metadata) {
        return new CouponInvalidException(status, code, message, cause, metadata);
    }
}
