package com.veggieshop.auth.exception.session;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a previously valid session has expired and is no longer usable.
 * Defaults to 401 Unauthorized.
 */
public final class SessionExpiredException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.UNAUTHORIZED;
    public static final String     DEFAULT_CODE    = "SESSION_EXPIRED";
    public static final String     DEFAULT_MESSAGE =
            "The session has expired. Please log in again.";

    /**
     * Full-argument constructor.
     */
    protected SessionExpiredException(HttpStatus status,
                                      String code,
                                      String message,
                                      Throwable cause,
                                      Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public SessionExpiredException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public SessionExpiredException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with metadata. */
    public SessionExpiredException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause. */
    public SessionExpiredException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public SessionExpiredException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    @Override
    public SessionExpiredException newInstance(HttpStatus status,
                                               String code,
                                               String message,
                                               Throwable cause,
                                               Map<String, Object> metadata) {
        return new SessionExpiredException(status, code, message, cause, metadata);
    }
}
