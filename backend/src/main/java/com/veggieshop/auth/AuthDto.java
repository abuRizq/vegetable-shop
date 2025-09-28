package com.veggieshop.auth;

import com.veggieshop.user.UserDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDto {

    // ==== LOGIN REQUEST ====
    @Data
    public static class AuthRequest {
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    // ==== LOGIN/REGISTER RESPONSE ====
    @Data
    public static class AuthResponse {
        private String token;           // Access token (JWT)
        private UserDto.UserResponse user;
    }

    // ==== REGISTRATION REQUEST ====
    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 64, message = "Password must be between 6 and 64 characters")
        private String password;
    }

    // ==== REFRESH TOKEN RESPONSE ====
    @Data
    public static class RefreshResponse {
        private String accessToken;     // New access token (JWT)
        private String userEmail;
    }

    // ==== FORGOT PASSWORD REQUEST ====
    @Data
    public static class ForgotPasswordRequest {
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        private String email;
    }

    // ==== RESET PASSWORD REQUEST ====
    @Data
    public static class ResetPasswordRequest {
        @NotBlank(message = "Reset token is required")
        private String token;

        @NotBlank(message = "New password is required")
        @Size(min = 6, max = 64, message = "Password must be between 6 and 64 characters")
        private String newPassword;
    }
}
