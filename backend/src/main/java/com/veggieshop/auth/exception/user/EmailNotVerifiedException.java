package com.veggieshop.auth.exception.user;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when a user attempts to perform an action that requires a verified email address,
 * but their email has not yet been verified.
 *
 * <p>Common scenarios:</p>
 * <ul>
 *   <li>Attempting to log in when email verification is mandatory.</li>
 *   <li>Trying to reset a password without a verified email.</li>
 *   <li>Accessing sensitive resources that require a verified identity.</li>
 * </ul>
 *
 * <p>According to RFC 7231, HTTP 403 Forbidden is returned to indicate
 * that the server understood the request but refuses to authorize it
 * until email verification is completed.</p>
 */
public class EmailNotVerifiedException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    private static final HttpStatus DEFAULT_STATUS = HttpStatus.FORBIDDEN;
    private static final String DEFAULT_CODE = "EMAIL_NOT_VERIFIED";
    private static final String DEFAULT_MESSAGE =
            "Your email address is not verified. Please verify your email before continuing.";

    /**
     * Creates a new EmailNotVerifiedException with the default message.
     */
    public EmailNotVerifiedException() {
        super(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE);
    }

    /**
     * Creates a new EmailNotVerifiedException with a custom message.
     *
     * @param message Human-readable detail message.
     */
    public EmailNotVerifiedException(String message) {
        super(DEFAULT_STATUS, DEFAULT_CODE, message);
    }

    /**
     * Creates a new EmailNotVerifiedException with a custom message and metadata.
     *
     * @param message  Human-readable detail message.
     * @param metadata Additional structured error context.
     */
    public EmailNotVerifiedException(String message, Map<String, Object> metadata) {
        super(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /**
     * Creates a new EmailNotVerifiedException with a custom message and cause.
     *
     * @param message Human-readable detail message.
     * @param cause   The underlying cause of the exception.
     */
    public EmailNotVerifiedException(String message, Throwable cause) {
        super(DEFAULT_STATUS, DEFAULT_CODE, message, cause);
    }

    /**
     * Creates a new EmailNotVerifiedException with only a cause.
     *
     * @param cause The underlying cause of the exception.
     */
    public EmailNotVerifiedException(Throwable cause) {
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
     * @return A new instance of EmailNotVerifiedException.
     */
    @Override
    public ApplicationException newInstance(
            HttpStatus status,
            String code,
            String message,
            Throwable cause,
            Map<String, Object> metadata) {

        EmailNotVerifiedException ex = new EmailNotVerifiedException(message, metadata);
        if (cause != null) {
            ex.initCause(cause);
        }
        return ex;
    }
}
