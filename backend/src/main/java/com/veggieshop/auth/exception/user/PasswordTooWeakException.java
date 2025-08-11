package com.veggieshop.auth.exception.user;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a user attempts to set a password that does not meet
 * the application's password strength requirements.
 *
 * <p>Typical scenarios include:</p>
 * <ul>
 *   <li>Password length is too short.</li>
 *   <li>Lack of required complexity (e.g., missing uppercase letters, digits, or special characters).</li>
 *   <li>Common or easily guessable passwords.</li>
 * </ul>
 *
 * <p>This maps to HTTP 422 Unprocessable Entity since the request is syntactically valid
 * but semantically unacceptable based on the application's password policy.</p>
 */
public class PasswordTooWeakException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    private static final HttpStatus DEFAULT_STATUS = HttpStatus.UNPROCESSABLE_ENTITY;
    private static final String DEFAULT_CODE = "PASSWORD_TOO_WEAK";
    private static final String DEFAULT_MESSAGE = "The provided password does not meet the required strength criteria.";

    /**
     * Creates a new PasswordTooWeakException with the default message.
     */
    public PasswordTooWeakException() {
        super(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE);
    }

    /**
     * Creates a new PasswordTooWeakException with a custom message.
     *
     * @param message Human-readable detail message.
     */
    public PasswordTooWeakException(String message) {
        super(DEFAULT_STATUS, DEFAULT_CODE, message);
    }

    /**
     * Creates a new PasswordTooWeakException with a custom message and metadata.
     *
     * @param message  Human-readable detail message.
     * @param metadata Additional structured error context.
     */
    public PasswordTooWeakException(String message, Map<String, Object> metadata) {
        super(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /**
     * Creates a new PasswordTooWeakException with a custom message and cause.
     *
     * @param message Human-readable detail message.
     * @param cause   The underlying cause of the exception.
     */
    public PasswordTooWeakException(String message, Throwable cause) {
        super(DEFAULT_STATUS, DEFAULT_CODE, message, cause);
    }

    /**
     * Creates a new PasswordTooWeakException with only a cause.
     *
     * @param cause The underlying cause of the exception.
     */
    public PasswordTooWeakException(Throwable cause) {
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
     * @return A new instance of PasswordTooWeakException.
     */
    @Override
    public ApplicationException newInstance(
            HttpStatus status,
            String code,
            String message,
            Throwable cause,
            Map<String, Object> metadata) {

        PasswordTooWeakException ex = new PasswordTooWeakException(message, metadata);
        if (cause != null) {
            ex.initCause(cause);
        }
        return ex;
    }
}
