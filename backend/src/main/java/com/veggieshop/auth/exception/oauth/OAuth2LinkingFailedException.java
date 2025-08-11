package com.veggieshop.auth.exception.oauth;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when an attempt to link a local user account with an external OAuth2 provider fails.
 * Defaults to 409 Conflict.
 */
public final class OAuth2LinkingFailedException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.CONFLICT;
    public static final String     DEFAULT_CODE    = "OAUTH2_LINKING_FAILED";
    public static final String     DEFAULT_MESSAGE =
            "Failed to link the OAuth2 provider account.";

    /**
     * Full-argument constructor.
     */
    protected OAuth2LinkingFailedException(HttpStatus status,
                                           String code,
                                           String message,
                                           Throwable cause,
                                           Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public OAuth2LinkingFailedException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public OAuth2LinkingFailedException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with cause. */
    public OAuth2LinkingFailedException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Custom message with metadata. */
    public OAuth2LinkingFailedException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause and metadata. */
    public OAuth2LinkingFailedException(String message, Throwable cause, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, metadata);
    }

    @Override
    public OAuth2LinkingFailedException newInstance(HttpStatus status,
                                                    String code,
                                                    String message,
                                                    Throwable cause,
                                                    Map<String, Object> metadata) {
        return new OAuth2LinkingFailedException(status, code, message, cause, metadata);
    }
}
