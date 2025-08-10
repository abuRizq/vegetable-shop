package com.veggieshop.auth;

import com.veggieshop.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    // 7 days in seconds
    public static final long REFRESH_TOKEN_VALIDITY_SECONDS = 7 * 24 * 60 * 60;

    /**
     * Create and persist a new refresh token for the user/device.
     * (You can enhance: invalidate previous tokens from same device if needed.)
     */
    @Override
    public RefreshToken createToken(User user, String deviceInfo) {
        String token = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plusSeconds(REFRESH_TOKEN_VALIDITY_SECONDS);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .deviceInfo(deviceInfo)
                .expiryDate(expiry)
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Validate a refresh token by value, check not revoked and not expired.
     */
    @Override
    public RefreshToken validateToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        if (refreshToken.isRevoked() || refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }
        return refreshToken;
    }

    /**
     * Revoke (invalidate) a specific refresh token.
     */
    @Override
    public void revokeToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);
        refreshTokenOpt.ifPresent(tokenEntity -> {
            tokenEntity.setRevoked(true);
            refreshTokenRepository.save(tokenEntity);
        });
    }

    /**
     * Revoke all refresh tokens for a specific user (logout everywhere).
     */
    @Override
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
