package com.veggieshop.service;

import com.veggieshop.dto.AuthDto;

public interface AuthService {
    AuthDto.AuthResponse login(AuthDto.AuthRequest request);
}
