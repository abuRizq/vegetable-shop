package com.veggieshop.user;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserDto {

    @Data
    public static class UserResponse {
        private Long id;
        private String name;
        private String email;
        private User.Role role;
        // Add fields like active, createdAt, updatedAt if needed
    }

    @Data
    public static class UserCreateRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 64, message = "Password must be between 6 and 64 characters")
        private String password;

        // Optional: for admin-controlled user creation
        private User.Role role;
    }

    @Data
    public static class UserUpdateRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        private String email;
    }

    @Data
    public static class PasswordChangeRequest {
        @NotBlank(message = "Old password is required")
        private String oldPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 6, max = 64, message = "New password must be between 6 and 64 characters")
        private String newPassword;
    }

    @Data
    public static class RoleChangeRequest {
        @NotNull(message = "Role is required")
        private User.Role role;
    }
}
