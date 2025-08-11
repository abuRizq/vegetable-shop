package com.veggieshop.auth.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when the authentication process cannot be completed temporarily.
 * Defaults to 503 Service Unavailable.
 */
public final class TemporaryAuthenticationException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.SERVICE_UNAVAILABLE;
    public static final String     DEFAULT_CODE    = "TEMPORARY_AUTHENTICATION_FAILURE";
    public static final String     DEFAULT_MESSAGE =
            "Authentication service is temporarily unavailable. Please try again later.";

    /**
     * Full-argument constructor.
     */
    protected TemporaryAuthenticationException(HttpStatus status,
                                               String code,
                                               String message,
                                               Throwable cause,
                                               Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public TemporaryAuthenticationException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public TemporaryAuthenticationException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with cause. */
    public TemporaryAuthenticationException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public TemporaryAuthenticationException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /** Custom message with metadata. */
    public TemporaryAuthenticationException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause and metadata. */
    public TemporaryAuthenticationException(String message, Throwable cause, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, metadata);
    }

    @Override
    public TemporaryAuthenticationException newInstance(HttpStatus status,
                                                        String code,
                                                        String message,
                                                        Throwable cause,
                                                        Map<String, Object> metadata) {
        return new TemporaryAuthenticationException(status, code, message, cause, metadata);
    }
}
