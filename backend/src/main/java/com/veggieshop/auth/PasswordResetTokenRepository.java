package com.veggieshop.auth;

import com.veggieshop.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // Find a reset token by its value
    Optional<PasswordResetToken> findByToken(String token);

    // Find all valid tokens for a user (for audit/cleanup)
    List<PasswordResetToken> findByUserAndUsedFalseAndExpiryDateAfter(User user, Instant now);

    // Optionally, delete all expired tokens for housekeeping
    void deleteByExpiryDateBefore(Instant expiryThreshold);

    // Optionally, delete all tokens for a user (for cleanup)
    void deleteByUser(User user);
}
