package com.veggieshop.auth.exception.session;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a session referenced by the client cannot be found in the system.
 * Defaults to 404 Not Found.
 */
public final class SessionNotFoundException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.NOT_FOUND;
    public static final String     DEFAULT_CODE    = "SESSION_NOT_FOUND";
    public static final String     DEFAULT_MESSAGE = "The requested session could not be found.";

    /**
     * Full-argument constructor.
     */
    protected SessionNotFoundException(HttpStatus status,
                                       String code,
                                       String message,
                                       Throwable cause,
                                       Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public SessionNotFoundException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public SessionNotFoundException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with metadata. */
    public SessionNotFoundException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause. */
    public SessionNotFoundException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public SessionNotFoundException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    @Override
    public SessionNotFoundException newInstance(HttpStatus status,
                                                String code,
                                                String message,
                                                Throwable cause,
                                                Map<String, Object> metadata) {
        return new SessionNotFoundException(status, code, message, cause, metadata);
    }
}
