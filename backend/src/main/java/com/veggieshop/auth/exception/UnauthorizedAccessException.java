package com.veggieshop.auth.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when an authenticated user attempts to access a resource
 * or perform an operation they are not authorized to use.
 * Defaults to 403 Forbidden.
 */
public final class UnauthorizedAccessException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.FORBIDDEN;
    public static final String     DEFAULT_CODE    = "UNAUTHORIZED_ACCESS";
    public static final String     DEFAULT_MESSAGE =
            "You are not authorized to access this resource";

    /**
     * Full-argument constructor.
     */
    protected UnauthorizedAccessException(HttpStatus status,
                                          String code,
                                          String message,
                                          Throwable cause,
                                          Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public UnauthorizedAccessException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public UnauthorizedAccessException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with cause. */
    public UnauthorizedAccessException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public UnauthorizedAccessException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /** Custom message with metadata. */
    public UnauthorizedAccessException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause and metadata. */
    public UnauthorizedAccessException(String message, Throwable cause, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, metadata);
    }

    @Override
    public UnauthorizedAccessException newInstance(HttpStatus status,
                                                   String code,
                                                   String message,
                                                   Throwable cause,
                                                   Map<String, Object> metadata) {
        return new UnauthorizedAccessException(status, code, message, cause, metadata);
    }
}
