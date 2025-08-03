package com.veggieshop.user;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDto {

    @Data
    public static class UserResponse {
        private Long id;
        private String name;
        private String email;
        private User.Role role;
    }

    @Data
    public static class UserCreateRequest {
        @NotBlank
        private String name;

        @Email
        @NotBlank
        private String email;

        @NotBlank
        @Size(min = 6, max = 64)
        private String password;

        private User.Role role; // optional for admin, default USER
    }

    @Data
    public static class UserUpdateRequest {
        @NotBlank
        private String name;

        @Email
        @NotBlank
        private String email;
    }
}
