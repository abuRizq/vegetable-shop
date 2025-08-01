package com.veggieshop.service;

import com.veggieshop.auth.AuthDto;

public interface AuthService {
    AuthDto.AuthResponse login(AuthDto.AuthRequest request);
}
