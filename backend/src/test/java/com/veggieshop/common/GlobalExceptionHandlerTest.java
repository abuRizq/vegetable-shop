package com.veggieshop.common;

import com.veggieshop.auth.exceptions.InvalidResetTokenException;
import com.veggieshop.exception.BadRequestException;
import com.veggieshop.exception.DuplicateException;
import com.veggieshop.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void handleNotFound_returnsNotFoundResponse() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User not found");

        ResponseEntity<ApiResponse<Object>> response = handler.handleNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isNotNull();
        assertThat(response.getBody().getError().getMessage()).contains("User not found");
    }

    @Test
    void handleBadRequest_returnsBadRequestResponse() {
        BadRequestException ex = new BadRequestException("Bad input");

        ResponseEntity<ApiResponse<Object>> response = handler.handleBadRequest(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError().getMessage()).contains("Bad input");
    }

    @Test
    void handleDuplicate_returnsConflictResponse() {
        DuplicateException ex = new DuplicateException("Duplicate email");

        ResponseEntity<ApiResponse<Object>> response = handler.handleDuplicate(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getError().getMessage()).contains("Duplicate email");
    }

    @Test
    void handleInvalidResetToken_returnsBadRequest() {
        InvalidResetTokenException ex = new InvalidResetTokenException("Invalid or expired token");

        ResponseEntity<ApiResponse<Object>> response = handler.handleInvalidResetToken(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError().getMessage()).contains("Invalid or expired token");
    }

    @Test
    void handleAccessDenied_returnsForbidden() {
        AccessDeniedException ex = new AccessDeniedException("Denied!");

        ResponseEntity<ApiResponse<Object>> response = handler.handleAccessDenied(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getError().getError()).isEqualTo("Forbidden");
        assertThat(response.getBody().getError().getMessage()).contains("not authorized");
    }

    @Test
    void handleValidation_returnsFieldErrors() {
        // Arrange
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("obj", "email", "Email is invalid");
        FieldError fieldError2 = new FieldError("obj", "name", "Name is required");
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException ex = Mockito.mock(MethodArgumentNotValidException.class);
        Mockito.when(ex.getBindingResult()).thenReturn(bindingResult);

        // Act
        ResponseEntity<ApiResponse<Object>> response = handler.handleValidation(ex, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiError error = response.getBody().getError();
        assertThat(error.getError()).isEqualTo("Validation Error");
        assertThat(error.getFieldErrors()).containsEntry("email", "Email is invalid");
        assertThat(error.getFieldErrors()).containsEntry("name", "Name is required");
        assertThat(error.getMessage()).contains("Validation failed");
    }

    @Test
    void handleAll_returnsInternalServerError() {
        Exception ex = new Exception("Something went wrong!");

        ResponseEntity<ApiResponse<Object>> response = handler.handleAll(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getError().getMessage()).contains("unexpected");
    }
}
