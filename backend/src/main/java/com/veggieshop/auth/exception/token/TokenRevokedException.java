package com.veggieshop.auth.exception.token;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when an authentication or authorization token has been revoked
 * and is no longer valid, even if not expired.
 * Defaults to 401 Unauthorized.
 */
public final class TokenRevokedException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.UNAUTHORIZED;
    public static final String     DEFAULT_CODE    = "TOKEN_REVOKED";
    public static final String     DEFAULT_MESSAGE = "The authentication token has been revoked.";

    /**
     * Full-argument constructor.
     */
    protected TokenRevokedException(HttpStatus status,
                                    String code,
                                    String message,
                                    Throwable cause,
                                    Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public TokenRevokedException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public TokenRevokedException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with metadata. */
    public TokenRevokedException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause. */
    public TokenRevokedException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public TokenRevokedException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    @Override
    public TokenRevokedException newInstance(HttpStatus status,
                                             String code,
                                             String message,
                                             Throwable cause,
                                             Map<String, Object> metadata) {
        return new TokenRevokedException(status, code, message, cause, metadata);
    }
}
