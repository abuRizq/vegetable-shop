package com.veggieshop.auth.exceptions;

/**
 * Thrown when a password reset token is invalid, expired, or has already been used.
 */
public class InvalidResetTokenException extends RuntimeException {

    /**
     * Constructs a new InvalidResetTokenException with the specified detail message.
     * @param message the detail message
     */
    public InvalidResetTokenException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidResetTokenException with the specified message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public InvalidResetTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new InvalidResetTokenException with the specified cause.
     * @param cause the cause
     */
    public InvalidResetTokenException(Throwable cause) {
        super(cause);
    }
}
