package com.veggieshop.inventory.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when reserving stock for an order or operation cannot be completed.
 * Defaults to 409 Conflict.
 */
public final class StockReservationFailedException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.CONFLICT;
    public static final String     DEFAULT_CODE    = "STOCK_RESERVATION_FAILED";
    public static final String     DEFAULT_MESSAGE = "Failed to reserve stock for the requested operation.";

    /**
     * Full-argument constructor.
     */
    protected StockReservationFailedException(HttpStatus status,
                                              String code,
                                              String message,
                                              Throwable cause,
                                              Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message (HTTP 409). */
    public StockReservationFailedException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message (HTTP 409). */
    public StockReservationFailedException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with metadata (HTTP 409). */
    public StockReservationFailedException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause (HTTP 409). */
    public StockReservationFailedException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause (HTTP 409). */
    public StockReservationFailedException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /** Allows custom HTTP status (e.g., 503) with metadata. */
    public StockReservationFailedException(HttpStatus status, String message, Map<String, Object> metadata) {
        this(status == null ? DEFAULT_STATUS : status, DEFAULT_CODE, message, null, metadata);
    }

    @Override
    public StockReservationFailedException newInstance(HttpStatus status,
                                                       String code,
                                                       String message,
                                                       Throwable cause,
                                                       Map<String, Object> metadata) {
        return new StockReservationFailedException(status, code, message, cause, metadata);
    }
}
