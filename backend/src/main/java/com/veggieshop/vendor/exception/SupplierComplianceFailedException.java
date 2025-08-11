package com.veggieshop.vendor.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a supplier fails to meet required compliance checks.
 * Defaults to HTTP 422 Unprocessable Entity.
 */
public final class SupplierComplianceFailedException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.UNPROCESSABLE_ENTITY; // 422
    public static final String     DEFAULT_CODE    = "SUPPLIER_COMPLIANCE_FAILED";
    public static final String     DEFAULT_MESSAGE = "Supplier compliance requirements have not been satisfied.";

    /**
     * Full-argument constructor (preserves all parameters exactly).
     */
    protected SupplierComplianceFailedException(HttpStatus status,
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
    public SupplierComplianceFailedException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message (HTTP 422). */
    public SupplierComplianceFailedException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message + metadata (HTTP 422). */
    public SupplierComplianceFailedException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message + cause (HTTP 422). */
    public SupplierComplianceFailedException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Cause-only with default message (HTTP 422). */
    public SupplierComplianceFailedException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /** Allow custom HTTP status (e.g., 403 or 409) while keeping same code. */
    public SupplierComplianceFailedException(HttpStatus status, String message, Map<String, Object> metadata) {
        this(status == null ? DEFAULT_STATUS : status, DEFAULT_CODE, message, null, metadata);
    }

    @Override
    public SupplierComplianceFailedException newInstance(HttpStatus status,
                                                         String code,
                                                         String message,
                                                         Throwable cause,
                                                         Map<String, Object> metadata) {
        return new SupplierComplianceFailedException(status, code, message, cause, metadata);
    }
}
