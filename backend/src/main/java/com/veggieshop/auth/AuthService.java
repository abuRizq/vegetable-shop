package com.veggieshop.auth;

import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    /**
     * Register a new user, generate JWT and refresh token,
     * and set refresh token in HttpOnly cookie.
     */
    AuthDto.AuthResponse register(AuthDto.RegisterRequest request, String deviceInfo, HttpServletResponse response);

    /**
     * Authenticate user, generate JWT and refresh token,
     * and set refresh token in HttpOnly cookie.
     */
    AuthDto.AuthResponse login(AuthDto.AuthRequest request, String deviceInfo, HttpServletResponse response);

    /**
     * Validate and rotate refresh token, generate new JWT,
     * set new refresh token in cookie, and return access token.
     */
    AuthDto.RefreshResponse refresh(String refreshTokenValue, String deviceInfo, HttpServletResponse response);

    /**
     * Logout the current session by revoking the refresh token.
     */
    void logout(String refreshTokenValue);

    /**
     * Send password reset link to user's email.
     */
    void sendResetPasswordLink(AuthDto.ForgotPasswordRequest request, String requestIp);

    /**
     * Reset password using the token and new password.
     */
    void resetPassword(AuthDto.ResetPasswordRequest request);
}
