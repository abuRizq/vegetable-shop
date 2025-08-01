package com.veggieshop.auth;

import com.veggieshop.user.UserDto;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDto {

    @Data
    public static class AuthRequest {
        @Email
        @NotBlank
        private String email;

        @NotBlank
        private String password;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private UserDto.UserResponse user;
    }
}
