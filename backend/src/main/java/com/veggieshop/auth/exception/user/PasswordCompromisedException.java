package com.veggieshop.auth.exception.user;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a user's chosen password is found in known data breaches or
 * appears on a compromised password list (e.g., "Have I Been Pwned" database).
 *
 * <p>Typical scenarios include:</p>
 * <ul>
 *   <li>Checking against a public password breach API or local compromised password list.</li>
 *   <li>Rejecting passwords that are known to be unsafe even if they meet complexity requirements.</li>
 * </ul>
 *
 * <p>This maps to HTTP 422 Unprocessable Entity since the request is syntactically
 * valid but semantically insecure.</p>
 */
public class PasswordCompromisedException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    private static final HttpStatus DEFAULT_STATUS = HttpStatus.UNPROCESSABLE_ENTITY;
    private static final String DEFAULT_CODE = "PASSWORD_COMPROMISED";
    private static final String DEFAULT_MESSAGE =
            "The provided password has been found in known data breaches and cannot be used for security reasons.";

    /**
     * Creates a new PasswordCompromisedException with the default message.
     */
    public PasswordCompromisedException() {
        super(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE);
    }

    /**
     * Creates a new PasswordCompromisedException with a custom message.
     *
     * @param message Human-readable detail message.
     */
    public PasswordCompromisedException(String message) {
        super(DEFAULT_STATUS, DEFAULT_CODE, message);
    }

    /**
     * Creates a new PasswordCompromisedException with a custom message and metadata.
     *
     * @param message  Human-readable detail message.
     * @param metadata Additional structured error context.
     */
    public PasswordCompromisedException(String message, Map<String, Object> metadata) {
        super(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /**
     * Creates a new PasswordCompromisedException with a custom message and cause.
     *
     * @param message Human-readable detail message.
     * @param cause   The underlying cause of the exception.
     */
    public PasswordCompromisedException(String message, Throwable cause) {
        super(DEFAULT_STATUS, DEFAULT_CODE, message, cause);
    }

    /**
     * Creates a new PasswordCompromisedException with only a cause.
     *
     * @param cause The underlying cause of the exception.
     */
    public PasswordCompromisedException(Throwable cause) {
        super(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause);
    }

    /**
     * Required by ApplicationException for reflective instantiation.
     *
     * @param status   HTTP status for the exception.
     * @param code     Machine-readable error code.
     * @param message  Human-readable error message.
     * @param cause    Root cause (if any).
     * @param metadata Additional contextual information.
     * @return A new instance of PasswordCompromisedException.
     */
    @Override
    public ApplicationException newInstance(
            HttpStatus status,
            String code,
            String message,
            Throwable cause,
            Map<String, Object> metadata) {

        PasswordCompromisedException ex = new PasswordCompromisedException(message, metadata);
        if (cause != null) {
            ex.initCause(cause);
        }
        return ex;
    }
}
