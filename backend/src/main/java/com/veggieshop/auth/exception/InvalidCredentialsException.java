package com.veggieshop.auth.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a user attempts to authenticate with invalid credentials
 * (e.g., wrong username/password or invalid authentication token).
 * Defaults to 401 Unauthorized.
 */
public final class InvalidCredentialsException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.UNAUTHORIZED;
    public static final String     DEFAULT_CODE    = "INVALID_CREDENTIALS";
    public static final String     DEFAULT_MESSAGE =
            "Invalid username, password, or authentication token.";

    /**
     * Full-argument constructor.
     */
    protected InvalidCredentialsException(HttpStatus status,
                                          String code,
                                          String message,
                                          Throwable cause,
                                          Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public InvalidCredentialsException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public InvalidCredentialsException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with cause. */
    public InvalidCredentialsException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public InvalidCredentialsException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /** Custom message with metadata. */
    public InvalidCredentialsException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause and metadata. */
    public InvalidCredentialsException(String message, Throwable cause, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, metadata);
    }

    @Override
    public InvalidCredentialsException newInstance(HttpStatus status,
                                                   String code,
                                                   String message,
                                                   Throwable cause,
                                                   Map<String, Object> metadata) {
        return new InvalidCredentialsException(status, code, message, cause, metadata);
    }
}
