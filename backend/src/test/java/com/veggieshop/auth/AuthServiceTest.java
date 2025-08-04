package com.veggieshop.auth;

import com.veggieshop.exception.BadRequestException;
import com.veggieshop.exception.DuplicateException;
import com.veggieshop.security.JwtUtil;
import com.veggieshop.user.User;
import com.veggieshop.user.UserMapper;
import com.veggieshop.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordResetTokenService passwordResetTokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Captor
    ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ==== register ====
    @Test
    void register_ShouldThrowDuplicateException_WhenEmailExists() {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setName("John");
        request.setEmail("john@example.com");
        request.setPassword("secret123");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() ->
                authService.register(request, "browser", mock(HttpServletResponse.class))
        ).isInstanceOf(DuplicateException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void register_ShouldCreateUserAndReturnToken_WhenEmailDoesNotExist() {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setName("Jane");
        request.setEmail("jane@example.com");
        request.setPassword("password");

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashedpw");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setId(10L); // simulate DB ID
            return u;
        });

        User savedUser = User.builder()
                .id(10L)
                .name("Jane")
                .email("jane@example.com")
                .password("hashedpw")
                .role(User.Role.USER)
                .build();

        when(userMapper.toUserResponse(any(User.class))).thenReturn(new com.veggieshop.user.UserDto.UserResponse());
        when(jwtUtil.generateAccessToken(any(UserDetails.class))).thenReturn("jwt-token");
        when(refreshTokenService.createToken(any(User.class), anyString()))
                .thenReturn(RefreshToken.builder().token("refresh-token").user(savedUser).build());

        HttpServletResponse response = mock(HttpServletResponse.class);

        AuthDto.AuthResponse result = authService.register(request, "browser", response);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getName()).isEqualTo("Jane");
        verify(refreshTokenService).createToken(any(User.class), eq("browser"));
    }

    // ==== login ====
    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsValid() {
        AuthDto.AuthRequest request = new AuthDto.AuthRequest();
        request.setEmail("login@mail.com");
        request.setPassword("pw");

        Authentication authentication = mock(Authentication.class);
        User user = User.builder()
                .id(5L)
                .email("login@mail.com")
                .password("hashedpw")
                .role(User.Role.USER)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("login@mail.com")).thenReturn(java.util.Optional.of(user));
        when(authentication.getPrincipal()).thenReturn(
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .build()
        );
        when(jwtUtil.generateAccessToken(any(UserDetails.class))).thenReturn("access-token");
        when(refreshTokenService.createToken(any(User.class), anyString()))
                .thenReturn(RefreshToken.builder().token("refreshtok").user(user).build());
        when(userMapper.toUserResponse(any(User.class))).thenReturn(new com.veggieshop.user.UserDto.UserResponse());

        HttpServletResponse response = mock(HttpServletResponse.class);

        AuthDto.AuthResponse result = authService.login(request, "Chrome", response);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("access-token");
        verify(refreshTokenService).createToken(user, "Chrome");
        verify(userMapper).toUserResponse(user);
    }

    @Test
    void login_ShouldThrowBadRequestException_WhenCredentialsInvalid() {
        AuthDto.AuthRequest request = new AuthDto.AuthRequest();
        request.setEmail("bad@user.com");
        request.setPassword("badpw");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad creds"));
        when(userRepository.findByEmail("bad@user.com")).thenReturn(java.util.Optional.empty());

        HttpServletResponse response = mock(HttpServletResponse.class);

        assertThatThrownBy(() -> authService.login(request, "Firefox", response))
                .isInstanceOf(BadCredentialsException.class);
    }

    // ==== refresh ====
    @Test
    void refresh_ShouldReturnNewAccessToken_WhenTokenValid() {
        RefreshToken token = RefreshToken.builder()
                .user(User.builder().id(1L).email("u@a.com").password("p").role(User.Role.USER).build())
                .token("old-token")
                .revoked(false)
                .expiryDate(java.time.Instant.now().plusSeconds(600))
                .build();

        when(refreshTokenService.validateToken("old-token")).thenReturn(token);
        when(refreshTokenService.createToken(any(User.class), anyString()))
                .thenReturn(RefreshToken.builder().token("new-token").user(token.getUser()).build());
        when(jwtUtil.generateAccessToken(any(UserDetails.class))).thenReturn("jwt-new-access");

        HttpServletResponse response = mock(HttpServletResponse.class);

        AuthDto.RefreshResponse resp = authService.refresh("old-token", "device", response);

        assertThat(resp).isNotNull();
        assertThat(resp.getAccessToken()).isEqualTo("jwt-new-access");
        verify(refreshTokenService).revokeToken("old-token");
        verify(refreshTokenService).createToken(token.getUser(), "device");
    }

    @Test
    void refresh_ShouldThrow_WhenTokenInvalid() {
        when(refreshTokenService.validateToken("bad-token"))
                .thenThrow(new IllegalArgumentException("Refresh token is expired or revoked"));

        HttpServletResponse response = mock(HttpServletResponse.class);

        assertThatThrownBy(() -> authService.refresh("bad-token", "device", response))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Refresh token is expired or revoked");
    }

    // ==== forgot password ====
    @Test
    void sendResetPasswordLink_ShouldCallEmailService_IfUserExists() {
        AuthDto.ForgotPasswordRequest req = new AuthDto.ForgotPasswordRequest();
        req.setEmail("foo@bar.com");

        User user = User.builder().id(9L).email("foo@bar.com").name("Foo").build();

        when(userRepository.findByEmail("foo@bar.com")).thenReturn(java.util.Optional.of(user));
        PasswordResetToken token = PasswordResetToken.builder().token("ttt").build();
        when(passwordResetTokenService.createToken(user, "127.0.0.1")).thenReturn(token);

        authService.sendResetPasswordLink(req, "127.0.0.1");

        verify(emailService).sendPasswordReset(eq("foo@bar.com"), eq("Foo"), contains("ttt"));
    }

    @Test
    void sendResetPasswordLink_ShouldNotThrow_IfUserNotExists() {
        AuthDto.ForgotPasswordRequest req = new AuthDto.ForgotPasswordRequest();
        req.setEmail("notfound@x.com");
        when(userRepository.findByEmail("notfound@x.com")).thenReturn(java.util.Optional.empty());

        assertThatCode(() -> authService.sendResetPasswordLink(req, "192.168.1.1"))
                .doesNotThrowAnyException();
        verify(emailService, never()).sendPasswordReset(anyString(), anyString(), anyString());
    }

    // ==== reset password ====
    @Test
    void resetPassword_ShouldChangePassword_AndRevokeAllTokens() {
        AuthDto.ResetPasswordRequest req = new AuthDto.ResetPasswordRequest();
        req.setToken("reset-token");
        req.setNewPassword("newpass");

        User user = User.builder().id(33L).email("bar@baz.com").password("oldhash").build();
        PasswordResetToken token = PasswordResetToken.builder()
                .token("reset-token")
                .user(user)
                .used(false)
                .build();

        when(passwordResetTokenService.validateToken("reset-token")).thenReturn(token);
        when(passwordEncoder.encode("newpass")).thenReturn("hashednewpass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0, User.class));

        doAnswer(inv -> {
            PasswordResetToken t = inv.getArgument(0);
            t.setUsed(true);
            return null;
        }).when(passwordResetTokenService).markTokenAsUsed(any(PasswordResetToken.class));

        authService.resetPassword(req);

        assertThat(token.isUsed()).isTrue();
        verify(passwordEncoder).encode("newpass");
        verify(refreshTokenService).revokeAllUserTokens(token.getUser());
        verify(passwordResetTokenService).markTokenAsUsed(token);
    }
}
