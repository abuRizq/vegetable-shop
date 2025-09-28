package com.veggieshop.auth;

import com.veggieshop.user.User;

public interface PasswordResetTokenService {

    /**
     * Create and save a new password reset token for the given user.
     * Invalidates previous unused tokens for this user.
     */
    PasswordResetToken createToken(User user, String requestIp);

    /**
     * Validates the given token, throws exception if invalid/expired/used.
     */
    PasswordResetToken validateToken(String token);

    /**
     * Mark the given token as used (after successful password reset).
     */
    void markTokenAsUsed(PasswordResetToken token);

    /**
     * Delete all expired tokens (housekeeping/cronjob).
     */
    void deleteExpiredTokens();
}
