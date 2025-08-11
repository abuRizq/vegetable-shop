package com.veggieshop.inventory.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when an operation cannot be completed because available inventory
 * is lower than the requested quantity. Defaults to 409 Conflict.
 */
public final class InsufficientStockException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.CONFLICT;
    public static final String     DEFAULT_CODE    = "INSUFFICIENT_STOCK";
    public static final String     DEFAULT_MESSAGE = "Insufficient stock to fulfill the request.";

    /**
     * Full-argument constructor.
     */
    protected InsufficientStockException(HttpStatus status,
                                         String code,
                                         String message,
                                         Throwable cause,
                                         Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public InsufficientStockException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public InsufficientStockException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with metadata (e.g., productId, sku, warehouseId, requestedQty, availableQty). */
    public InsufficientStockException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause. */
    public InsufficientStockException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public InsufficientStockException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    @Override
    public InsufficientStockException newInstance(HttpStatus status,
                                                  String code,
                                                  String message,
                                                  Throwable cause,
                                                  Map<String, Object> metadata) {
        return new InsufficientStockException(status, code, message, cause, metadata);
    }
}
