package com.veggieshop.auth.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class InvalidResetTokenExceptionTest {

    @Test
    void shouldCreateWithMessage() {
        String message = "Token is invalid";
        InvalidResetTokenException ex = new InvalidResetTokenException(message);

        assertThat(ex).isInstanceOf(InvalidResetTokenException.class);
        assertThat(ex.getMessage()).isEqualTo(message);
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void shouldCreateWithMessageAndCause() {
        String message = "Token expired";
        Throwable cause = new RuntimeException("Root cause");
        InvalidResetTokenException ex = new InvalidResetTokenException(message, cause);

        assertThat(ex.getMessage()).isEqualTo(message);
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void shouldCreateWithCauseOnly() {
        Throwable cause = new IllegalStateException("Underlying");
        InvalidResetTokenException ex = new InvalidResetTokenException(cause);

        assertThat(ex.getMessage()).contains("Underlying"); // Standard RuntimeException message
        assertThat(ex.getCause()).isEqualTo(cause);
    }
}
