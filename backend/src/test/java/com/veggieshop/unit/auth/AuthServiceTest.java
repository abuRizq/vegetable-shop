package com.veggieshop.unit.auth;

import com.veggieshop.auth.*;
import com.veggieshop.auth.dto.AuthDto;
import com.veggieshop.auth.token.RefreshToken;
import com.veggieshop.auth.token.RefreshTokenService;
import com.veggieshop.exception.BadRequestException;
import com.veggieshop.exception.DuplicateException;
import com.veggieshop.security.forDelete.JwtUtil;
import com.veggieshop.user.User;
import com.veggieshop.user.dto.UserDto;
import com.veggieshop.user.mapper.UserMapper;
import com.veggieshop.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private PasswordResetTokenService passwordResetTokenService;
    @Mock private EmailService emailService;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock private HttpServletResponse response;

    private AutoCloseable closeable;

    private final User user = User.builder()
            .id(1L)
            .name("Regular User")
            .email("user@veggieshop.com")
            .password("hashedpw")
            .role(User.Role.USER)
            .enabled(true)
            .createdAt(Instant.now())
            .build();

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        // Set reflection values for @Value fields
        TestUtils.setField(authService, "resetLinkBase", "https://test.com/reset/");
        TestUtils.setField(authService, "cookieSecure", false);
        TestUtils.setField(authService, "refreshTokenValiditySeconds", 3600);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // ============= Register =============
    @Test
    void register_shouldSucceed_whenEmailNotExists() {
        AuthDto.RegisterRequest req = new AuthDto.RegisterRequest();
        req.setName("Regular User");
        req.setEmail("user@veggieshop.com");
        req.setPassword("password");

        when(userRepository.existsByEmail("user@veggieshop.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashedpw");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail()).password(user.getPassword()).roles("USER").build();
        when(jwtUtil.generateAccessToken(any(UserDetails.class))).thenReturn("accesstoken");
        RefreshToken refreshToken = RefreshToken.builder().token("refreshtoken").user(user).expiryDate(Instant.now().plusSeconds(3600)).build();
        when(refreshTokenService.createToken(user, "chrome")).thenReturn(refreshToken);

        UserDto.UserResponse userResponse = new UserDto.UserResponse();
        userResponse.setId(1L); userResponse.setEmail("user@veggieshop.com");
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        AuthDto.AuthResponse result = authService.register(req, "chrome", response);

        assertThat(result.getToken()).isEqualTo("accesstoken");
        assertThat(result.getUser().getEmail()).isEqualTo("user@veggieshop.com");
        verify(response).addCookie(any(Cookie.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrow_whenEmailExists() {
        AuthDto.RegisterRequest req = new AuthDto.RegisterRequest();
        req.setName("Regular User");
        req.setEmail("user@veggieshop.com");
        req.setPassword("password");

        when(userRepository.existsByEmail("user@veggieshop.com")).thenReturn(true);
        assertThrows(DuplicateException.class, () -> authService.register(req, "chrome", response));
        verify(userRepository, never()).save(any());
    }

    // ============= Login =============
    @Test
    void login_shouldSucceed_whenCredentialsCorrect() {
        AuthDto.AuthRequest req = new AuthDto.AuthRequest();
        req.setEmail("user@veggieshop.com");
        req.setPassword("password");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail()).password(user.getPassword()).roles("USER").build();

        when(userRepository.findByEmail("user@veggieshop.com")).thenReturn(Optional.of(user));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateAccessToken(userDetails)).thenReturn("accesstoken");
        RefreshToken refreshToken = RefreshToken.builder().token("refreshtoken").user(user).expiryDate(Instant.now().plusSeconds(3600)).build();
        when(refreshTokenService.createToken(user, "chrome")).thenReturn(refreshToken);

        UserDto.UserResponse userResponse = new UserDto.UserResponse();
        userResponse.setId(1L); userResponse.setEmail("user@veggieshop.com");
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        AuthDto.AuthResponse result = authService.login(req, "chrome", response);

        assertThat(result.getToken()).isEqualTo("accesstoken");
        assertThat(result.getUser().getEmail()).isEqualTo("user@veggieshop.com");
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    void login_shouldThrow_whenUserNotFound() {
        AuthDto.AuthRequest req = new AuthDto.AuthRequest();
        req.setEmail("notfound@veggieshop.com");
        req.setPassword("password");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        when(userRepository.findByEmail("notfound@veggieshop.com")).thenReturn(Optional.empty());
        when(authentication.getPrincipal()).thenReturn(mock(UserDetails.class));

        assertThrows(BadRequestException.class, () -> authService.login(req, "chrome", response));
    }

    // ============= Refresh Token =============
    @Test
    void refresh_shouldSucceed_whenTokenValid() {
        RefreshToken oldToken = RefreshToken.builder().token("oldtoken").user(user).expiryDate(Instant.now().plusSeconds(3600)).build();
        RefreshToken newToken = RefreshToken.builder().token("newtoken").user(user).expiryDate(Instant.now().plusSeconds(3600)).build();

        when(refreshTokenService.validateToken("oldtoken")).thenReturn(oldToken);
        doNothing().when(refreshTokenService).revokeToken("oldtoken");
        when(refreshTokenService.createToken(user, "chrome")).thenReturn(newToken);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail()).password(user.getPassword()).roles("USER").build();
        when(jwtUtil.generateAccessToken(userDetails)).thenReturn("accesstoken");

        AuthDto.RefreshResponse result = authService.refresh("oldtoken", "chrome", response);

        assertThat(result.getAccessToken()).isEqualTo("accesstoken");
        assertThat(result.getUserEmail()).isEqualTo("user@veggieshop.com");
        verify(response).addCookie(any(Cookie.class));
    }

    // ============= Logout =============
    @Test
    void logout_shouldRevokeToken() {
        doNothing().when(refreshTokenService).revokeToken("refreshtoken");
        authService.logout("refreshtoken");
        verify(refreshTokenService).revokeToken("refreshtoken");
    }

    // ============= Send Reset Password Link =============
    @Test
    void sendResetPasswordLink_shouldSendEmail_whenUserExists() {
        when(userRepository.findByEmail("user@veggieshop.com")).thenReturn(Optional.of(user));
        PasswordResetToken token = PasswordResetToken.builder().token("resettoken").user(user).expiryDate(Instant.now().plusSeconds(900)).build();
        when(passwordResetTokenService.createToken(user, "127.0.0.1")).thenReturn(token);
        doNothing().when(emailService).sendPasswordReset(eq("user@veggieshop.com"), eq("Regular User"), contains("resettoken"));

        authService.sendResetPasswordLink(new AuthDto.ForgotPasswordRequest() {{
            setEmail("user@veggieshop.com");
        }}, "127.0.0.1");

        verify(emailService).sendPasswordReset(eq("user@veggieshop.com"), eq("Regular User"), contains("resettoken"));
    }

    @Test
    void sendResetPasswordLink_shouldNotThrow_whenUserNotFound() {
        when(userRepository.findByEmail("notfound@veggieshop.com")).thenReturn(Optional.empty());
        // No exception should be thrown!
        authService.sendResetPasswordLink(new AuthDto.ForgotPasswordRequest() {{
            setEmail("notfound@veggieshop.com");
        }}, "127.0.0.1");
        verify(emailService, never()).sendPasswordReset(any(), any(), any());
    }

    // ============= Reset Password =============
    @Test
    void resetPassword_shouldSucceed_whenTokenValid() {
        PasswordResetToken token = PasswordResetToken.builder().token("resettoken").user(user).expiryDate(Instant.now().plusSeconds(900)).build();
        when(passwordResetTokenService.validateToken("resettoken")).thenReturn(token);
        when(passwordEncoder.encode("newpass")).thenReturn("hashednewpass");
        when(userRepository.save(user)).thenReturn(user);
        doNothing().when(passwordResetTokenService).markTokenAsUsed(token);
        doNothing().when(refreshTokenService).revokeAllUserTokens(user);

        AuthDto.ResetPasswordRequest req = new AuthDto.ResetPasswordRequest();
        req.setToken("resettoken");
        req.setNewPassword("newpass");

        authService.resetPassword(req);

        verify(passwordResetTokenService).markTokenAsUsed(token);
        verify(refreshTokenService).revokeAllUserTokens(user);
        verify(userRepository).save(user);
        assertThat(user.getPassword()).isEqualTo("hashednewpass");
    }
}