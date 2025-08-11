package com.veggieshop.core.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a request or action violates a defined security policy.
 * Defaults to 403 Forbidden.
 */
public final class SecurityPolicyViolationException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.FORBIDDEN;
    public static final String     DEFAULT_CODE    = "SECURITY_POLICY_VIOLATION";
    public static final String     DEFAULT_MESSAGE = "Access denied due to security policy violation.";

    /**
     * Full-argument constructor.
     */
    protected SecurityPolicyViolationException(HttpStatus status,
                                               String code,
                                               String message,
                                               Throwable cause,
                                               Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public SecurityPolicyViolationException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public SecurityPolicyViolationException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with cause. */
    public SecurityPolicyViolationException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Default message with cause. */
    public SecurityPolicyViolationException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    /** Custom message with metadata. */
    public SecurityPolicyViolationException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause and metadata. */
    public SecurityPolicyViolationException(String message, Throwable cause, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, metadata);
    }

    /** Custom error code with message. */
    public SecurityPolicyViolationException(String errorCode, String message) {
        this(DEFAULT_STATUS, errorCode, message, null, null);
    }

    @Override
    public SecurityPolicyViolationException newInstance(HttpStatus status,
                                                        String code,
                                                        String message,
                                                        Throwable cause,
                                                        Map<String, Object> metadata) {
        return new SecurityPolicyViolationException(status, code, message, cause, metadata);
    }
}
