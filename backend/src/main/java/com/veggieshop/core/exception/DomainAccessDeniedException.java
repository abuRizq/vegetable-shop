package com.veggieshop.core.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when the current principal is not allowed to perform the requested action.
 * Defaults to 403 Forbidden.
 */
public final class DomainAccessDeniedException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.FORBIDDEN;
    public static final String     DEFAULT_CODE    = "ACCESS_DENIED";
    public static final String     DEFAULT_MESSAGE = "Access denied.";

    /**
     * Full-argument constructor.
     */
    protected DomainAccessDeniedException(HttpStatus status,
                                          String code,
                                          String message,
                                          Throwable cause,
                                          Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public DomainAccessDeniedException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public DomainAccessDeniedException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with cause. */
    public DomainAccessDeniedException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public DomainAccessDeniedException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /** Custom message with metadata. */
    public DomainAccessDeniedException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause and metadata. */
    public DomainAccessDeniedException(String message, Throwable cause, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, metadata);
    }

    /** Custom error code with message. */
    public DomainAccessDeniedException(String errorCode, String message) {
        this(DEFAULT_STATUS, errorCode, message, null, null);
    }

    // ---------- Convenience factories ----------

    /** Missing authority/permission (e.g., "order:write"). */
    public static DomainAccessDeniedException missingPermission(String permission) {
        return new DomainAccessDeniedException("Required permission is missing: " + permission,
                Map.of("requiredPermission", permission));
    }

    /** Missing role (e.g., "ROLE_ADMIN"). */
    public static DomainAccessDeniedException roleRequired(String role) {
        return new DomainAccessDeniedException("Required role is missing: " + role,
                Map.of("requiredRole", role));
    }

    /** Ownership/tenant constraint violation. */
    public static DomainAccessDeniedException ownershipViolation(String resource, Object id) {
        return new DomainAccessDeniedException("Access denied to resource.",
                Map.of("resource", resource, "id", String.valueOf(id), "reason", "ownership_violation"));
    }

    @Override
    public DomainAccessDeniedException newInstance(HttpStatus status,
                                                   String code,
                                                   String message,
                                                   Throwable cause,
                                                   Map<String, Object> metadata) {
        return new DomainAccessDeniedException(status, code, message, cause, metadata);
    }
}
