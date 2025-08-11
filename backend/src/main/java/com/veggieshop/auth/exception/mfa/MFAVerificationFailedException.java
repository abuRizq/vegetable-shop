package com.veggieshop.auth.exception.mfa;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a user's Multi-Factor Authentication (MFA) verification attempt fails.
 * Defaults to 401 Unauthorized.
 */
public final class MFAVerificationFailedException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.UNAUTHORIZED;
    public static final String     DEFAULT_CODE    = "MFA_VERIFICATION_FAILED";
    public static final String     DEFAULT_MESSAGE =
            "Multi-Factor Authentication verification failed.";

    /**
     * Full-argument constructor.
     */
    protected MFAVerificationFailedException(HttpStatus status,
                                             String code,
                                             String message,
                                             Throwable cause,
                                             Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public MFAVerificationFailedException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public MFAVerificationFailedException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with metadata. */
    public MFAVerificationFailedException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause. */
    public MFAVerificationFailedException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public MFAVerificationFailedException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    @Override
    public MFAVerificationFailedException newInstance(HttpStatus status,
                                                      String code,
                                                      String message,
                                                      Throwable cause,
                                                      Map<String, Object> metadata) {
        return new MFAVerificationFailedException(status, code, message, cause, metadata);
    }
}
