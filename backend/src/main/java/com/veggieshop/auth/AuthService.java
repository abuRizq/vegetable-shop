package com.veggieshop.auth;

public interface AuthService {
    AuthDto.AuthResponse login(AuthDto.AuthRequest request);
}
