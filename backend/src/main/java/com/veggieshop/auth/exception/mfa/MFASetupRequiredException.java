package com.veggieshop.auth.exception.mfa;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when an account must complete MFA setup before proceeding.
 * Defaults to 403 Forbidden.
 */
public final class MFASetupRequiredException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.FORBIDDEN;
    public static final String     DEFAULT_CODE    = "MFA_SETUP_REQUIRED";
    public static final String     DEFAULT_MESSAGE =
            "Multi-Factor Authentication setup is required before continuing.";

    /**
     * Full-argument constructor.
     */
    protected MFASetupRequiredException(HttpStatus status,
                                        String code,
                                        String message,
                                        Throwable cause,
                                        Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public MFASetupRequiredException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public MFASetupRequiredException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with metadata. */
    public MFASetupRequiredException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause. */
    public MFASetupRequiredException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public MFASetupRequiredException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    @Override
    public MFASetupRequiredException newInstance(HttpStatus status,
                                                 String code,
                                                 String message,
                                                 Throwable cause,
                                                 Map<String, Object> metadata) {
        return new MFASetupRequiredException(status, code, message, cause, metadata);
    }
}
