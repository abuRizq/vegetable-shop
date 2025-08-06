package com.veggieshop.auth;

import com.veggieshop.exception.InvalidResetTokenException;
import com.veggieshop.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private static final long TOKEN_EXPIRY_SECONDS = 15 * 60; // 15 minutes
    private final PasswordResetTokenRepository tokenRepository;

    @Override
    @Transactional
    public PasswordResetToken createToken(User user, String requestIp) {
        // Invalidate previous unused tokens for this user (best practice)
        tokenRepository.findByUserAndUsedFalseAndExpiryDateAfter(user, Instant.now())
                .forEach(t -> {
                    t.setUsed(true);
                    t.setUsedAt(Instant.now());
                });

        // Generate a secure, random token (base64-url)
        String token = generateSecureToken();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .expiryDate(Instant.now().plusSeconds(TOKEN_EXPIRY_SECONDS))
                .used(false)
                .user(user)
                .requestIp(requestIp)
                .build();

        return tokenRepository.save(resetToken);
    }

    @Override
    public PasswordResetToken validateToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidResetTokenException("Invalid or expired reset token."));

        if (resetToken.isUsed()) {
            throw new InvalidResetTokenException("Reset token has already been used.");
        }
        if (resetToken.isExpired()) {
            throw new InvalidResetTokenException("Reset token is expired.");
        }
        return resetToken;
    }

    @Override
    public void markTokenAsUsed(PasswordResetToken token) {
        token.setUsed(true);
        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        tokenRepository.deleteByExpiryDateBefore(Instant.now());
    }

    // === Secure random string (base64-url, 48 bytes ~ 64 chars) ===
    private String generateSecureToken() {
        byte[] randomBytes = new byte[48];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
