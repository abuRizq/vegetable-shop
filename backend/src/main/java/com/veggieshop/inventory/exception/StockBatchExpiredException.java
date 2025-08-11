package com.veggieshop.inventory.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when attempting to use or allocate stock from an expired batch.
 * Defaults to 410 Gone.
 */
public final class StockBatchExpiredException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.GONE; // 410 Gone
    public static final String     DEFAULT_CODE    = "STOCK_BATCH_EXPIRED";
    public static final String     DEFAULT_MESSAGE = "The stock batch has expired and cannot be used.";

    /**
     * Full-argument constructor.
     */
    protected StockBatchExpiredException(HttpStatus status,
                                         String code,
                                         String message,
                                         Throwable cause,
                                         Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public StockBatchExpiredException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public StockBatchExpiredException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with metadata (e.g., batchId, sku, warehouseId, expiryDate, attemptedQty). */
    public StockBatchExpiredException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause. */
    public StockBatchExpiredException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public StockBatchExpiredException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    @Override
    public StockBatchExpiredException newInstance(HttpStatus status,
                                                  String code,
                                                  String message,
                                                  Throwable cause,
                                                  Map<String, Object> metadata) {
        return new StockBatchExpiredException(status, code, message, cause, metadata);
    }
}
