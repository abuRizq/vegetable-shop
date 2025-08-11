package com.veggieshop.auth.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a user account exists but has not been activated/verified yet.
 * Defaults to 403 Forbidden.
 */
public final class AccountNotActivatedException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.FORBIDDEN;
    public static final String     DEFAULT_CODE    = "ACCOUNT_NOT_ACTIVATED";
    public static final String     DEFAULT_MESSAGE =
            "Your account is not activated. Please complete the activation process to proceed.";

    /**
     * Full-argument constructor.
     */
    protected AccountNotActivatedException(HttpStatus status,
                                           String code,
                                           String message,
                                           Throwable cause,
                                           Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public AccountNotActivatedException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public AccountNotActivatedException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with metadata. */
    public AccountNotActivatedException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause. */
    public AccountNotActivatedException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public AccountNotActivatedException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    @Override
    public AccountNotActivatedException newInstance(HttpStatus status,
                                                    String code,
                                                    String message,
                                                    Throwable cause,
                                                    Map<String, Object> metadata) {
        return new AccountNotActivatedException(status, code, message, cause, metadata);
    }
}
