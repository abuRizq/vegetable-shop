package com.veggieshop.auth.exception.token;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a provided authentication or authorization token is invalid.
 * Defaults to 401 Unauthorized.
 */
public final class InvalidTokenException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.UNAUTHORIZED;
    public static final String     DEFAULT_CODE    = "INVALID_TOKEN";
    public static final String     DEFAULT_MESSAGE = "The provided token is invalid.";

    /**
     * Full-argument constructor.
     */
    protected InvalidTokenException(HttpStatus status,
                                    String code,
                                    String message,
                                    Throwable cause,
                                    Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public InvalidTokenException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public InvalidTokenException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with metadata. */
    public InvalidTokenException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause. */
    public InvalidTokenException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public InvalidTokenException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    @Override
    public InvalidTokenException newInstance(HttpStatus status,
                                             String code,
                                             String message,
                                             Throwable cause,
                                             Map<String, Object> metadata) {
        return new InvalidTokenException(status, code, message, cause, metadata);
    }
}
