package com.veggieshop.auth.exception.mfa;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when Multi-Factor Authentication (MFA) verification is required
 * before completing the authentication process or a sensitive operation.
 * Defaults to 401 Unauthorized.
 */
public final class MFARequiredException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.UNAUTHORIZED;
    public static final String     DEFAULT_CODE    = "MFA_REQUIRED";
    public static final String     DEFAULT_MESSAGE =
            "Multi-Factor Authentication is required to proceed.";

    /**
     * Full-argument constructor.
     */
    protected MFARequiredException(HttpStatus status,
                                   String code,
                                   String message,
                                   Throwable cause,
                                   Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public MFARequiredException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public MFARequiredException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with metadata. */
    public MFARequiredException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause. */
    public MFARequiredException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public MFARequiredException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    @Override
    public MFARequiredException newInstance(HttpStatus status,
                                            String code,
                                            String message,
                                            Throwable cause,
                                            Map<String, Object> metadata) {
        return new MFARequiredException(status, code, message, cause, metadata);
    }
}
