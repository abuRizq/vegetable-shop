package com.veggieshop.auth;

import com.veggieshop.auth.exceptions.InvalidResetTokenException;
import com.veggieshop.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordResetTokenServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @InjectMocks
    private PasswordResetTokenServiceImpl tokenService;

    @Captor
    ArgumentCaptor<PasswordResetToken> tokenCaptor;

    private final User user = User.builder()
            .id(1L)
            .email("reset@user.com")
            .name("Reset User")
            .build();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createToken_ShouldInvalidateOldTokensAndCreateNew() {
        PasswordResetToken oldToken = PasswordResetToken.builder()
                .id(2L)
                .user(user)
                .used(false)
                .expiryDate(Instant.now().plusSeconds(600))
                .build();

        when(tokenRepository.findByUserAndUsedFalseAndExpiryDateAfter(eq(user), any()))
                .thenReturn(List.of(oldToken));
        when(tokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PasswordResetToken newToken = tokenService.createToken(user, "127.0.0.1");

        // Old token should be marked used
        assertThat(oldToken.isUsed()).isTrue();
        assertThat(oldToken.getUsedAt()).isNotNull();

        // New token fields
        assertThat(newToken.getToken()).isNotBlank();
        assertThat(newToken.getUser()).isEqualTo(user);
        assertThat(newToken.getExpiryDate()).isAfter(Instant.now());
        assertThat(newToken.isUsed()).isFalse();
        assertThat(newToken.getRequestIp()).isEqualTo("127.0.0.1");
        verify(tokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void validateToken_ShouldReturnToken_WhenValid() {
        PasswordResetToken validToken = PasswordResetToken.builder()
                .token("tok123")
                .user(user)
                .used(false)
                .expiryDate(Instant.now().plusSeconds(1000))
                .build();

        when(tokenRepository.findByToken("tok123")).thenReturn(Optional.of(validToken));

        PasswordResetToken result = tokenService.validateToken("tok123");
        assertThat(result).isSameAs(validToken);
    }

    @Test
    void validateToken_ShouldThrow_WhenTokenNotFound() {
        when(tokenRepository.findByToken("notfound")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tokenService.validateToken("notfound"))
                .isInstanceOf(InvalidResetTokenException.class)
                .hasMessageContaining("Invalid or expired reset token");
    }

    @Test
    void validateToken_ShouldThrow_WhenTokenExpired() {
        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .token("expired")
                .user(user)
                .used(false)
                .expiryDate(Instant.now().minusSeconds(10))
                .build();

        when(tokenRepository.findByToken("expired")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> tokenService.validateToken("expired"))
                .isInstanceOf(InvalidResetTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void validateToken_ShouldThrow_WhenTokenUsed() {
        PasswordResetToken usedToken = PasswordResetToken.builder()
                .token("used123")
                .user(user)
                .used(true)
                .expiryDate(Instant.now().plusSeconds(500))
                .build();

        when(tokenRepository.findByToken("used123")).thenReturn(Optional.of(usedToken));

        assertThatThrownBy(() -> tokenService.validateToken("used123"))
                .isInstanceOf(InvalidResetTokenException.class)
                .hasMessageContaining("already been used");
    }

    @Test
    void markTokenAsUsed_ShouldSetUsedAndUsedAt() {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("markme")
                .used(false)
                .build();

        when(tokenRepository.save(token)).thenReturn(token);

        tokenService.markTokenAsUsed(token);

        assertThat(token.isUsed()).isTrue();
        assertThat(token.getUsedAt()).isNotNull();
        verify(tokenRepository).save(token);
    }

    @Test
    void deleteExpiredTokens_ShouldCallRepositoryDelete() {
        doNothing().when(tokenRepository).deleteByExpiryDateBefore(any());

        tokenService.deleteExpiredTokens();

        verify(tokenRepository).deleteByExpiryDateBefore(any(Instant.class));
    }
}
