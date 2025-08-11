package com.veggieshop.media.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when an uploaded file exceeds the configured maximum size limit.
 * Defaults to 413 Payload Too Large.
 */
public final class FileTooLargeException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.PAYLOAD_TOO_LARGE; // 413
    public static final String     DEFAULT_CODE    = "FILE_TOO_LARGE";
    public static final String     DEFAULT_MESSAGE =
            "Uploaded file exceeds the maximum allowed size.";

    /**
     * Full-argument constructor.
     */
    protected FileTooLargeException(HttpStatus status,
                                    String code,
                                    String message,
                                    Throwable cause,
                                    Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message (HTTP 413). */
    public FileTooLargeException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message (HTTP 413). */
    public FileTooLargeException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with metadata (HTTP 413). */
    public FileTooLargeException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause (HTTP 413). */
    public FileTooLargeException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause (HTTP 413). */
    public FileTooLargeException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /** Custom message with cause and metadata (HTTP 413). */
    public FileTooLargeException(String message, Throwable cause, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, metadata);
    }

    /** Allow custom HTTP status (e.g., 422) with same error code. */
    public FileTooLargeException(HttpStatus status, String message, Map<String, Object> metadata) {
        this(status == null ? DEFAULT_STATUS : status, DEFAULT_CODE, message, null, metadata);
    }

    @Override
    public FileTooLargeException newInstance(HttpStatus status,
                                             String code,
                                             String message,
                                             Throwable cause,
                                             Map<String, Object> metadata) {
        return new FileTooLargeException(status, code, message, cause, metadata);
    }
}
