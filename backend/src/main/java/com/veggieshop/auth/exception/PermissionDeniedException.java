package com.veggieshop.auth.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a user is authenticated but attempts an action
 * for which they lack the required permission or role.
 * Defaults to 403 Forbidden.
 */
public final class PermissionDeniedException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.FORBIDDEN;
    public static final String     DEFAULT_CODE    = "PERMISSION_DENIED";
    public static final String     DEFAULT_MESSAGE =
            "You do not have permission to perform this action";

    /**
     * Full-argument constructor.
     */
    protected PermissionDeniedException(HttpStatus status,
                                        String code,
                                        String message,
                                        Throwable cause,
                                        Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public PermissionDeniedException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public PermissionDeniedException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with cause. */
    public PermissionDeniedException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public PermissionDeniedException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /** Custom message with metadata. */
    public PermissionDeniedException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause and metadata. */
    public PermissionDeniedException(String message, Throwable cause, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, metadata);
    }

    @Override
    public PermissionDeniedException newInstance(HttpStatus status,
                                                 String code,
                                                 String message,
                                                 Throwable cause,
                                                 Map<String, Object> metadata) {
        return new PermissionDeniedException(status, code, message, cause, metadata);
    }
}
