package com.veggieshop.auth.exception.user;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a user attempts to change their password but the provided
 * old password does not match the one stored in the system.
 *
 * <p>Typical usage:</p>
 * <ul>
 *   <li>During a password change flow, validating the old password before setting a new one.</li>
 *   <li>Preventing unauthorized password changes by requiring confirmation of the current password.</li>
 * </ul>
 *
 * <p>This maps to HTTP 400 Bad Request as the request is valid syntactically,
 * but contains incorrect user-provided data.</p>
 */
public class OldPasswordIncorrectException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    private static final HttpStatus DEFAULT_STATUS = HttpStatus.BAD_REQUEST;
    private static final String DEFAULT_CODE = "OLD_PASSWORD_INCORRECT";
    private static final String DEFAULT_MESSAGE = "The old password provided is incorrect.";

    /**
     * Creates a new OldPasswordIncorrectException with the default message.
     */
    public OldPasswordIncorrectException() {
        super(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE);
    }

    /**
     * Creates a new OldPasswordIncorrectException with a custom message.
     *
     * @param message Human-readable detail message.
     */
    public OldPasswordIncorrectException(String message) {
        super(DEFAULT_STATUS, DEFAULT_CODE, message);
    }

    /**
     * Creates a new OldPasswordIncorrectException with a custom message and metadata.
     *
     * @param message  Human-readable detail message.
     * @param metadata Additional structured error context.
     */
    public OldPasswordIncorrectException(String message, Map<String, Object> metadata) {
        super(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /**
     * Creates a new OldPasswordIncorrectException with a custom message and cause.
     *
     * @param message Human-readable detail message.
     * @param cause   The underlying cause of the exception.
     */
    public OldPasswordIncorrectException(String message, Throwable cause) {
        super(DEFAULT_STATUS, DEFAULT_CODE, message, cause);
    }

    /**
     * Creates a new OldPasswordIncorrectException with only a cause.
     *
     * @param cause The underlying cause of the exception.
     */
    public OldPasswordIncorrectException(Throwable cause) {
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
     * @return A new instance of OldPasswordIncorrectException.
     */
    @Override
    public ApplicationException newInstance(
            HttpStatus status,
            String code,
            String message,
            Throwable cause,
            Map<String, Object> metadata) {

        OldPasswordIncorrectException ex = new OldPasswordIncorrectException(message, metadata);
        if (cause != null) {
            ex.initCause(cause);
        }
        return ex;
    }
}
