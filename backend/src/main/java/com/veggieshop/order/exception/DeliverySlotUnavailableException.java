package com.veggieshop.order.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when the requested delivery slot cannot be reserved or has no remaining capacity.
 * Defaults to HTTP 409 Conflict.
 */
public final class DeliverySlotUnavailableException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.CONFLICT; // 409
    public static final String     DEFAULT_CODE    = "DELIVERY_SLOT_UNAVAILABLE";
    public static final String     DEFAULT_MESSAGE = "The requested delivery slot is unavailable.";

    /**
     * Full-argument constructor (preserves all parameters exactly).
     */
    protected DeliverySlotUnavailableException(HttpStatus status,
                                               String code,
                                               String message,
                                               Throwable cause,
                                               Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Default constructor (HTTP 409). */
    public DeliverySlotUnavailableException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message (HTTP 409). */
    public DeliverySlotUnavailableException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message + metadata (HTTP 409). */
    public DeliverySlotUnavailableException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message + cause (HTTP 409). */
    public DeliverySlotUnavailableException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Cause-only with default message (HTTP 409). */
    public DeliverySlotUnavailableException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /** Allow custom HTTP status (e.g., 503) while keeping same code. */
    public DeliverySlotUnavailableException(HttpStatus status, String message, Map<String, Object> metadata) {
        this(status == null ? DEFAULT_STATUS : status, DEFAULT_CODE, message, null, metadata);
    }

    @Override
    public DeliverySlotUnavailableException newInstance(HttpStatus status,
                                                        String code,
                                                        String message,
                                                        Throwable cause,
                                                        Map<String, Object> metadata) {
        return new DeliverySlotUnavailableException(status, code, message, cause, metadata);
    }
}
