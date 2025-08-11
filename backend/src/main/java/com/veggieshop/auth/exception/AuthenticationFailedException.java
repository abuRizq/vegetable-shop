package com.veggieshop.auth.exception;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thrown when authentication fails for any generic reason
 * (invalid credentials, inactive account, unknown user, etc.).
 * Defaults to 401 Unauthorized.
 */
public final class AuthenticationFailedException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.UNAUTHORIZED;
    public static final String     DEFAULT_CODE    = "AUTHENTICATION_FAILED";
    public static final String     DEFAULT_MESSAGE = "Authentication failed. Please check your credentials.";

    /**
     * Full-argument constructor.
     */
    protected AuthenticationFailedException(HttpStatus status,
                                            String code,
                                            String message,
                                            Throwable cause,
                                            Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public AuthenticationFailedException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public AuthenticationFailedException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with cause. */
    public AuthenticationFailedException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public AuthenticationFailedException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /** Custom message with metadata. */
    public AuthenticationFailedException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause and metadata. */
    public AuthenticationFailedException(String message, Throwable cause, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, metadata);
    }

    /** Factory with username + optional client IP. */
    public static AuthenticationFailedException forUser(String username, String clientIp) {
        Map<String, Object> meta = new LinkedHashMap<>();
        if (username != null) meta.put("username", username);
        if (clientIp != null) meta.put("clientIp", clientIp);
        return new AuthenticationFailedException(DEFAULT_MESSAGE, meta);
    }

    /** Factory with username only. */
    public static AuthenticationFailedException forUser(String username) {
        return forUser(username, null);
    }

    @Override
    public AuthenticationFailedException newInstance(HttpStatus status,
                                                     String code,
                                                     String message,
                                                     Throwable cause,
                                                     Map<String, Object> metadata) {
        return new AuthenticationFailedException(status, code, message, cause, metadata);
    }
}
