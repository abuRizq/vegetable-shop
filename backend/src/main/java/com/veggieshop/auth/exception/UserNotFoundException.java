package com.veggieshop.auth.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a user account cannot be found in the system.
 * Defaults to 404 Not Found.
 */
public final class UserNotFoundException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.NOT_FOUND;
    public static final String     DEFAULT_CODE    = "USER_NOT_FOUND";
    public static final String     DEFAULT_MESSAGE =
            "The specified user could not be found.";

    /**
     * Full-argument constructor.
     */
    protected UserNotFoundException(HttpStatus status,
                                    String code,
                                    String message,
                                    Throwable cause,
                                    Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public UserNotFoundException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public UserNotFoundException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with cause. */
    public UserNotFoundException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public UserNotFoundException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /** Custom message with metadata. */
    public UserNotFoundException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause and metadata. */
    public UserNotFoundException(String message, Throwable cause, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, metadata);
    }

    @Override
    public UserNotFoundException newInstance(HttpStatus status,
                                             String code,
                                             String message,
                                             Throwable cause,
                                             Map<String, Object> metadata) {
        return new UserNotFoundException(status, code, message, cause, metadata);
    }
}
