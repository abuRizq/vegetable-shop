package com.veggieshop.media.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when media processing (encode/thumbnail/extract/scan) fails.
 * Defaults to 500 Internal Server Error.
 */
public final class MediaProcessingFailedException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.INTERNAL_SERVER_ERROR; // 500
    public static final String     DEFAULT_CODE    = "MEDIA_PROCESSING_FAILED";
    public static final String     DEFAULT_MESSAGE = "Media processing failed due to an internal error.";

    /**
     * Full-argument constructor.
     */
    protected MediaProcessingFailedException(HttpStatus status,
                                             String code,
                                             String message,
                                             Throwable cause,
                                             Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message (HTTP 500). */
    public MediaProcessingFailedException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message (HTTP 500). */
    public MediaProcessingFailedException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with cause (HTTP 500). */
    public MediaProcessingFailedException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause (HTTP 500). */
    public MediaProcessingFailedException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /** Custom message with metadata (e.g., mediaId, operation, processor, reason). */
    public MediaProcessingFailedException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause and metadata. */
    public MediaProcessingFailedException(String message, Throwable cause, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, metadata);
    }

    /** Allow custom HTTP status (e.g., 422/503/507) with the same error code. */
    public MediaProcessingFailedException(HttpStatus status, String message, Map<String, Object> metadata) {
        this(status == null ? DEFAULT_STATUS : status, DEFAULT_CODE, message, null, metadata);
    }

    @Override
    public MediaProcessingFailedException newInstance(HttpStatus status,
                                                      String code,
                                                      String message,
                                                      Throwable cause,
                                                      Map<String, Object> metadata) {
        return new MediaProcessingFailedException(status, code, message, cause, metadata);
    }
}
