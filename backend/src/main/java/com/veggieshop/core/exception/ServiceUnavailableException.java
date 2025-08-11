package com.veggieshop.core.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a requested service is temporarily unavailable.
 * Defaults to 503 Service Unavailable.
 */
public final class ServiceUnavailableException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.SERVICE_UNAVAILABLE;
    public static final String     DEFAULT_CODE    = "SERVICE_UNAVAILABLE";
    public static final String     DEFAULT_MESSAGE =
            "The service is temporarily unavailable. Please try again later.";

    /**
     * Full-argument constructor.
     */
    protected ServiceUnavailableException(HttpStatus status,
                                          String code,
                                          String message,
                                          Throwable cause,
                                          Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public ServiceUnavailableException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public ServiceUnavailableException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with cause. */
    public ServiceUnavailableException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public ServiceUnavailableException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /** Custom message with metadata. */
    public ServiceUnavailableException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause and metadata. */
    public ServiceUnavailableException(String message, Throwable cause, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, metadata);
    }

    /** Custom error code with message. */
    public ServiceUnavailableException(String errorCode, String message) {
        this(DEFAULT_STATUS, errorCode, message, null, null);
    }

    @Override
    public ServiceUnavailableException newInstance(HttpStatus status,
                                                   String code,
                                                   String message,
                                                   Throwable cause,
                                                   Map<String, Object> metadata) {
        return new ServiceUnavailableException(status, code, message, cause, metadata);
    }
}
