package com.veggieshop.auth;

import com.veggieshop.user.User;

public interface RefreshTokenService {

    /**
     * Create a new refresh token for a user/device.
     */
    RefreshToken createToken(User user, String deviceInfo);

    /**
     * Validate a refresh token (not revoked or expired).
     * Throws exception if invalid.
     */
    RefreshToken validateToken(String token);

    /**
     * Revoke (invalidate) a refresh token (logout a session).
     */
    void revokeToken(String token);

    /**
     * Revoke all refresh tokens for a user (logout all sessions).
     */
    void revokeAllUserTokens(User user);
}
