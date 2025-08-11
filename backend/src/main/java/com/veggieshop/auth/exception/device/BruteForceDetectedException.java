package com.veggieshop.auth.exception.device;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Raised when multiple consecutive failed authentication attempts are detected.
 * Defaults to 429 Too Many Requests.
 */
public final class BruteForceDetectedException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.TOO_MANY_REQUESTS;
    public static final String     DEFAULT_CODE    = "BRUTE_FORCE_DETECTED";
    public static final String     DEFAULT_MESSAGE =
            "Too many failed login attempts detected. Please try again later.";

    /**
     * Full-argument constructor used for cloning and complete control.
     */
    protected BruteForceDetectedException(HttpStatus status,
                                          String code,
                                          String message,
                                          Throwable cause,
                                          Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public BruteForceDetectedException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public BruteForceDetectedException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with cause. */
    public BruteForceDetectedException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Custom message with metadata. */
    public BruteForceDetectedException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause and metadata. */
    public BruteForceDetectedException(String message, Throwable cause, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, metadata);
    }

    @Override
    public BruteForceDetectedException newInstance(HttpStatus status,
                                                   String code,
                                                   String message,
                                                   Throwable cause,
                                                   Map<String, Object> metadata) {
        return new BruteForceDetectedException(status, code, message, cause, metadata);
    }
}
