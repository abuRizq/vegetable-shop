package com.veggieshop.auth.exception.device;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a login or sensitive operation is attempted from an unrecognized device.
 * Defaults to 403 Forbidden.
 */
public final class DeviceNotRecognizedException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.FORBIDDEN;
    public static final String     DEFAULT_CODE    = "DEVICE_NOT_RECOGNIZED";
    public static final String     DEFAULT_MESSAGE = "Access denied: unrecognized device.";

    /**
     * Full-argument constructor.
     */
    protected DeviceNotRecognizedException(HttpStatus status,
                                           String code,
                                           String message,
                                           Throwable cause,
                                           Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public DeviceNotRecognizedException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public DeviceNotRecognizedException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with metadata. */
    public DeviceNotRecognizedException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause. */
    public DeviceNotRecognizedException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public DeviceNotRecognizedException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    @Override
    public DeviceNotRecognizedException newInstance(HttpStatus status,
                                                    String code,
                                                    String message,
                                                    Throwable cause,
                                                    Map<String, Object> metadata) {
        return new DeviceNotRecognizedException(status, code, message, cause, metadata);
    }
}
