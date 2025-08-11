package com.veggieshop.core.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when the client has sent too many requests in a given time frame.
 * Defaults to 429 Too Many Requests.
 */
public final class RateLimitExceededException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.TOO_MANY_REQUESTS;
    public static final String     DEFAULT_CODE    = "RATE_LIMIT_EXCEEDED";
    public static final String     DEFAULT_MESSAGE = "Too many requests. Please try again later.";

    /**
     * Full-argument constructor.
     */
    protected RateLimitExceededException(HttpStatus status,
                                         String code,
                                         String message,
                                         Throwable cause,
                                         Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public RateLimitExceededException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public RateLimitExceededException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with cause. */
    public RateLimitExceededException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public RateLimitExceededException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /** Custom message with metadata (e.g., {"retryAfter":"30s"}). */
    public RateLimitExceededException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause and metadata. */
    public RateLimitExceededException(String message, Throwable cause, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, metadata);
    }

    /** Custom error code with message. */
    public RateLimitExceededException(String errorCode, String message) {
        this(DEFAULT_STATUS, errorCode, message, null, null);
    }

    @Override
    public RateLimitExceededException newInstance(HttpStatus status,
                                                  String code,
                                                  String message,
                                                  Throwable cause,
                                                  Map<String, Object> metadata) {
        return new RateLimitExceededException(status, code, message, cause, metadata);
    }
}
