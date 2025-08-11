package com.veggieshop.auth.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

/**
 * Base class for all authentication/authorization exceptions in the auth module.
 * Associates exceptions with {@link AuthErrorCode} and standardizes status/message/metadata.
 */
public abstract class AuthException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    /**
     * Full-argument constructor used by all convenience constructors.
     */
    protected AuthException(AuthErrorCode errorCode,
                            HttpStatus status,
                            String message,
                            Throwable cause,
                            Map<String, Object> metadata) {
        super(status, errorCode.getCode(), message, cause, metadata);
    }

    /** Uses defaults from the error code. */
    protected AuthException(AuthErrorCode errorCode) {
        this(errorCode, errorCode.getHttpStatus(), errorCode.getDefaultMessage(), null, null);
    }

    /** Custom message, default status from the error code. */
    protected AuthException(AuthErrorCode errorCode, String message) {
        this(errorCode, errorCode.getHttpStatus(), message, null, null);
    }

    /** Custom message + cause, default status from the error code. */
    protected AuthException(AuthErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, errorCode.getHttpStatus(), message, cause, null);
    }

    /** Custom message + cause + metadata, default status from the error code. */
    protected AuthException(AuthErrorCode errorCode, String message, Throwable cause, Map<String, Object> metadata) {
        this(errorCode, errorCode.getHttpStatus(), message, cause, metadata);
    }

    /** Default message from code + cause. */
    protected AuthException(AuthErrorCode errorCode, Throwable cause) {
        this(errorCode, errorCode.getHttpStatus(), errorCode.getDefaultMessage(), cause, null);
    }

    /** Default message from code + cause + metadata. */
    protected AuthException(AuthErrorCode errorCode, Throwable cause, Map<String, Object> metadata) {
        this(errorCode, errorCode.getHttpStatus(), errorCode.getDefaultMessage(), cause, metadata);
    }

    /** Utility for creating immutable metadata singletons. */
    protected static Map<String, Object> metadataOf(String key, Object value) {
        return Collections.singletonMap(key, value);
    }
}
