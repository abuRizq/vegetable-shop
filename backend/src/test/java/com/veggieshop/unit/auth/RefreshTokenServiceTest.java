package com.veggieshop.unit.auth;

import com.veggieshop.auth.RefreshToken;
import com.veggieshop.auth.RefreshTokenRepository;
import com.veggieshop.auth.RefreshTokenServiceImpl;
import com.veggieshop.user.User;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private AutoCloseable closeable;

    private final User user = User.builder()
            .id(5L)
            .email("user@veggieshop.com")
            .build();

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void createToken_shouldPersistAndReturnToken() {
        // Arrange
        String deviceInfo = "Chrome";
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RefreshToken result = refreshTokenService.createToken(user, deviceInfo);

        // Assert
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        RefreshToken saved = tokenCaptor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getDeviceInfo()).isEqualTo(deviceInfo);
        assertThat(saved.getToken()).isNotBlank();
        assertThat(saved.getExpiryDate()).isAfter(Instant.now());
        assertThat(result.getToken()).isEqualTo(saved.getToken());
    }

    @Test
    void validateToken_shouldReturnToken_whenValid() {
        String tokenValue = "token-123";
        RefreshToken token = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .revoked(false)
                .expiryDate(Instant.now().plusSeconds(3000))
                .build();
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

        RefreshToken result = refreshTokenService.validateToken(tokenValue);
        assertThat(result).isEqualTo(token);
    }

    @Test
    void validateToken_shouldThrow_whenNotFound() {
        when(refreshTokenRepository.findByToken("notfound")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> refreshTokenService.validateToken("notfound"));
    }

    @Test
    void validateToken_shouldThrow_whenRevoked() {
        String tokenValue = "revoked-token";
        RefreshToken token = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .revoked(true)
                .expiryDate(Instant.now().plusSeconds(1000))
                .build();
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));
        assertThrows(IllegalArgumentException.class, () -> refreshTokenService.validateToken(tokenValue));
    }

    @Test
    void validateToken_shouldThrow_whenExpired() {
        String tokenValue = "expired-token";
        RefreshToken token = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .revoked(false)
                .expiryDate(Instant.now().minusSeconds(10))
                .build();
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));
        assertThrows(IllegalArgumentException.class, () -> refreshTokenService.validateToken(tokenValue));
    }

    @Test
    void revokeToken_shouldSetRevokedTrue_whenTokenExists() {
        String tokenValue = "revoke-me";
        RefreshToken token = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .revoked(false)
                .expiryDate(Instant.now().plusSeconds(1000))
                .build();
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));
        when(refreshTokenRepository.save(token)).thenReturn(token);

        refreshTokenService.revokeToken(tokenValue);

        assertThat(token.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void revokeToken_shouldNotThrow_whenTokenNotFound() {
        when(refreshTokenRepository.findByToken("notfound")).thenReturn(Optional.empty());
        // Should not throw
        refreshTokenService.revokeToken("notfound");
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void revokeAllUserTokens_shouldDeleteByUser() {
        refreshTokenService.revokeAllUserTokens(user);
        verify(refreshTokenRepository).deleteByUser(user);
    }
}
