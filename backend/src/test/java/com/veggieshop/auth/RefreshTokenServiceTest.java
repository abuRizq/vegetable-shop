package com.veggieshop.auth;

import com.veggieshop.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private final User user = User.builder()
            .id(42L)
            .email("user@email.com")
            .name("Test User")
            .build();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createToken_ShouldPersistNewToken() {
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RefreshToken token = refreshTokenService.createToken(user, "My-Device");

        assertThat(token).isNotNull();
        assertThat(token.getUser()).isEqualTo(user);
        assertThat(token.getDeviceInfo()).isEqualTo("My-Device");
        assertThat(token.getToken()).isNotBlank();
        assertThat(token.isRevoked()).isFalse();
        assertThat(token.getExpiryDate()).isAfter(Instant.now());

        verify(refreshTokenRepository).save(token);
    }

    @Test
    void validateToken_ShouldReturnToken_WhenValid() {
        RefreshToken token = RefreshToken.builder()
                .token("token-valid")
                .user(user)
                .revoked(false)
                .expiryDate(Instant.now().plusSeconds(300))
                .build();

        when(refreshTokenRepository.findByToken("token-valid")).thenReturn(Optional.of(token));

        RefreshToken result = refreshTokenService.validateToken("token-valid");
        assertThat(result).isEqualTo(token);
    }

    @Test
    void validateToken_ShouldThrow_WhenNotFound() {
        when(refreshTokenRepository.findByToken("not-found")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.validateToken("not-found"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void validateToken_ShouldThrow_WhenRevoked() {
        RefreshToken token = RefreshToken.builder()
                .token("revoked-token")
                .user(user)
                .revoked(true)
                .expiryDate(Instant.now().plusSeconds(600))
                .build();

        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.validateToken("revoked-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expired or revoked");
    }

    @Test
    void validateToken_ShouldThrow_WhenExpired() {
        RefreshToken token = RefreshToken.builder()
                .token("expired-token")
                .user(user)
                .revoked(false)
                .expiryDate(Instant.now().minusSeconds(10))
                .build();

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.validateToken("expired-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expired or revoked");
    }

    @Test
    void revokeToken_ShouldMarkAsRevoked_IfExists() {
        RefreshToken token = RefreshToken.builder()
                .token("torevoke")
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("torevoke")).thenReturn(Optional.of(token));
        when(refreshTokenRepository.save(token)).thenReturn(token);

        refreshTokenService.revokeToken("torevoke");

        assertThat(token.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void revokeToken_ShouldDoNothing_IfNotExists() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        // Should NOT throw
        refreshTokenService.revokeToken("missing");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void revokeAllUserTokens_ShouldCallRepositoryDeleteByUser() {
        doNothing().when(refreshTokenRepository).deleteByUser(user);

        refreshTokenService.revokeAllUserTokens(user);

        verify(refreshTokenRepository).deleteByUser(user);
    }
}
