package com.veggieshop.auth.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a user account is locked and cannot be used to authenticate or perform actions.
 * Defaults to 423 Locked.
 */
public final class AccountLockedException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.LOCKED; // 423
    public static final String     DEFAULT_CODE    = "ACCOUNT_LOCKED";
    public static final String     DEFAULT_MESSAGE =
            "Your account is locked. Please contact support to regain access.";

    /**
     * Full-argument constructor.
     */
    protected AccountLockedException(HttpStatus status,
                                     String code,
                                     String message,
                                     Throwable cause,
                                     Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public AccountLockedException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public AccountLockedException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with metadata. */
    public AccountLockedException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause. */
    public AccountLockedException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public AccountLockedException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    @Override
    public AccountLockedException newInstance(HttpStatus status,
                                              String code,
                                              String message,
                                              Throwable cause,
                                              Map<String, Object> metadata) {
        return new AccountLockedException(status, code, message, cause, metadata);
    }
}
