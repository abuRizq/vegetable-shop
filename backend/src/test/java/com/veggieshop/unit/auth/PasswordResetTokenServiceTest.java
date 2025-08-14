package com.veggieshop.unit.auth;

import com.veggieshop.auth.PasswordResetToken;
import com.veggieshop.auth.PasswordResetTokenRepository;
import com.veggieshop.auth.PasswordResetTokenServiceImpl;
import com.veggieshop.exception.InvalidResetTokenException;
import com.veggieshop.user.User;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PasswordResetTokenServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @InjectMocks
    private PasswordResetTokenServiceImpl tokenService;

    private AutoCloseable closeable;

    private final User user = User.builder()
            .id(1L)
            .email("test@user.com")
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
    void createToken_shouldCreateAndInvalidateOldTokens() {
        String requestIp = "127.0.0.1";
        PasswordResetToken oldToken = PasswordResetToken.builder()
                .id(10L)
                .user(user)
                .expiryDate(Instant.now().plusSeconds(1000))
                .used(false)
                .build();

        when(tokenRepository.findByUserAndUsedFalseAndExpiryDateAfter(eq(user), any(Instant.class)))
                .thenReturn(List.of(oldToken));
        when(tokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PasswordResetToken result = tokenService.createToken(user, requestIp);

        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getToken()).isNotBlank();
        assertThat(result.isUsed()).isFalse();
        assertThat(result.getRequestIp()).isEqualTo(requestIp);
        assertThat(result.getExpiryDate()).isAfter(Instant.now());

        // Old tokens must be marked used
        assertThat(oldToken.isUsed()).isTrue();
        assertThat(oldToken.getUsedAt()).isNotNull();
    }

    @Test
    void validateToken_shouldReturnValidToken() {
        String tokenValue = "tok123";
        PasswordResetToken token = PasswordResetToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(Instant.now().plusSeconds(500))
                .used(false)
                .build();
        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));
        PasswordResetToken result = tokenService.validateToken(tokenValue);

        assertThat(result).isEqualTo(token);
    }

    @Test
    void validateToken_shouldThrow_whenTokenNotFound() {
        when(tokenRepository.findByToken("notfound")).thenReturn(Optional.empty());
        assertThrows(InvalidResetTokenException.class, () -> tokenService.validateToken("notfound"));
    }

    @Test
    void validateToken_shouldThrow_whenTokenUsed() {
        String tokenValue = "usedtoken";
        PasswordResetToken token = PasswordResetToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(Instant.now().plusSeconds(500))
                .used(true)
                .build();
        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));
        assertThrows(InvalidResetTokenException.class, () -> tokenService.validateToken(tokenValue));
    }

    @Test
    void validateToken_shouldThrow_whenTokenExpired() {
        String tokenValue = "expiredtoken";
        PasswordResetToken token = PasswordResetToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(Instant.now().minusSeconds(10))
                .used(false)
                .build();
        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));
        assertThrows(InvalidResetTokenException.class, () -> tokenService.validateToken(tokenValue));
    }

    @Test
    void markTokenAsUsed_shouldSetUsedAndUsedAt() {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("any")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(500))
                .used(false)
                .build();

        when(tokenRepository.save(token)).thenReturn(token);

        tokenService.markTokenAsUsed(token);

        assertThat(token.isUsed()).isTrue();
        assertThat(token.getUsedAt()).isNotNull();
        verify(tokenRepository).save(token);
    }

    @Test
    void deleteExpiredTokens_shouldCallRepository() {
        tokenService.deleteExpiredTokens();
        verify(tokenRepository).deleteByExpiryDateBefore(any(Instant.class));
    }
}
