package com.veggieshop.order.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when an order state transition violates the domain/state machine rules.
 * Defaults to HTTP 409 Conflict.
 */
public final class OrderStatusTransitionIllegalException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.CONFLICT; // 409
    public static final String     DEFAULT_CODE    = "ORDER_STATUS_TRANSITION_ILLEGAL";
    public static final String     DEFAULT_MESSAGE = "Illegal order status transition attempted.";

    /**
     * Full-argument constructor (preserves all parameters exactly).
     */
    protected OrderStatusTransitionIllegalException(HttpStatus status,
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

    /** Default constructor (HTTP 409). */
    public OrderStatusTransitionIllegalException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message (HTTP 409). */
    public OrderStatusTransitionIllegalException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message + metadata (HTTP 409). */
    public OrderStatusTransitionIllegalException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message + cause (HTTP 409). */
    public OrderStatusTransitionIllegalException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Cause-only with default message (HTTP 409). */
    public OrderStatusTransitionIllegalException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    @Override
    public OrderStatusTransitionIllegalException newInstance(HttpStatus status,
                                                             String code,
                                                             String message,
                                                             Throwable cause,
                                                             Map<String, Object> metadata) {
        return new OrderStatusTransitionIllegalException(status, code, message, cause, metadata);
    }
}
