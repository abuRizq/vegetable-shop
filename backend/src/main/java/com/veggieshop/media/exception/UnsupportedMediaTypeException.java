package com.veggieshop.media.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when the provided content type is not supported for the requested operation.
 * Defaults to HTTP 415 Unsupported Media Type.
 */
public final class UnsupportedMediaTypeException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.UNSUPPORTED_MEDIA_TYPE; // 415
    public static final String     DEFAULT_CODE    = "UNSUPPORTED_MEDIA_TYPE";
    public static final String     DEFAULT_MESSAGE = "The provided media type is not supported.";

    /**
     * Full-argument constructor (preserves all values exactly).
     */
    protected UnsupportedMediaTypeException(HttpStatus status,
                                            String code,
                                            String message,
                                            Throwable cause,
                                            Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Default constructor (HTTP 415). */
    public UnsupportedMediaTypeException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message (HTTP 415). */
    public UnsupportedMediaTypeException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message + metadata (HTTP 415). */
    public UnsupportedMediaTypeException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message + cause (HTTP 415). */
    public UnsupportedMediaTypeException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Cause-only with default message (HTTP 415). */
    public UnsupportedMediaTypeException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /** Allow custom HTTP status (e.g., 422) while keeping same code. */
    public UnsupportedMediaTypeException(HttpStatus status, String message, Map<String, Object> metadata) {
        this(status == null ? DEFAULT_STATUS : status, DEFAULT_CODE, message, null, metadata);
    }

    @Override
    public UnsupportedMediaTypeException newInstance(HttpStatus status,
                                                     String code,
                                                     String message,
                                                     Throwable cause,
                                                     Map<String, Object> metadata) {
        return new UnsupportedMediaTypeException(status, code, message, cause, metadata);
    }
}
