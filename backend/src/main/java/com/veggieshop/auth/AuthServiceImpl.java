package com.veggieshop.auth;

import com.veggieshop.exception.BadRequestException;
import com.veggieshop.exception.DuplicateException;
import com.veggieshop.security.JwtUtil;
import com.veggieshop.user.User;
import com.veggieshop.user.UserRepository;
import com.veggieshop.user.UserMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private static final String RESET_LINK_BASE = "https://your-frontend.com/reset-password?token=";

    @Override
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request, String deviceInfo, HttpServletResponse response) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateException("Email already exists");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .build();
        user = userRepository.save(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

        String accessToken = jwtUtil.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createToken(user, deviceInfo);

        setRefreshTokenCookie(response, refreshToken.getToken());

        AuthDto.AuthResponse authResponse = new AuthDto.AuthResponse();
        authResponse.setToken(accessToken);
        authResponse.setUser(userMapper.toUserResponse(user));
        return authResponse;
    }

    @Override
    public AuthDto.AuthResponse login(AuthDto.AuthRequest request, String deviceInfo, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        String accessToken = jwtUtil.generateAccessToken((UserDetails) authentication.getPrincipal());
        RefreshToken refreshToken = refreshTokenService.createToken(user, deviceInfo);

        setRefreshTokenCookie(response, refreshToken.getToken());

        AuthDto.AuthResponse resp = new AuthDto.AuthResponse();
        resp.setToken(accessToken);
        resp.setUser(userMapper.toUserResponse(user));
        return resp;
    }

    @Override
    public AuthDto.RefreshResponse refresh(String refreshTokenValue, String deviceInfo, HttpServletResponse response) {
        RefreshToken existingToken = refreshTokenService.validateToken(refreshTokenValue);

        refreshTokenService.revokeToken(existingToken.getToken());
        RefreshToken newToken = refreshTokenService.createToken(existingToken.getUser(), deviceInfo);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(existingToken.getUser().getEmail())
                .password(existingToken.getUser().getPassword())
                .roles(existingToken.getUser().getRole().name())
                .build();
        String newAccessToken = jwtUtil.generateAccessToken(userDetails);

        setRefreshTokenCookie(response, newToken.getToken());

        AuthDto.RefreshResponse resp = new AuthDto.RefreshResponse();
        resp.setAccessToken(newAccessToken);
        resp.setUserEmail(existingToken.getUser().getEmail());
        return resp;
    }

    @Override
    public void logout(String refreshTokenValue) {
        refreshTokenService.revokeToken(refreshTokenValue);
        // Frontend should clear the cookie
    }

    @Override
    public void sendResetPasswordLink(AuthDto.ForgotPasswordRequest request, String requestIp) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            PasswordResetToken token = passwordResetTokenService.createToken(user, requestIp);
            String resetLink = RESET_LINK_BASE + token.getToken();
            emailService.sendPasswordReset(user.getEmail(), user.getName(), resetLink);
        });
        // Always respond with success, never reveal if email exists or not
    }

    @Override
    public void resetPassword(AuthDto.ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenService.validateToken(request.getToken());
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        passwordResetTokenService.markTokenAsUsed(token);

        // (Optional) Logout from all devices after password reset
        refreshTokenService.revokeAllUserTokens(user);
    }

    // == Helper: set refresh token as HttpOnly, Secure cookie ==
    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // enable only in production
        cookie.setPath("/");
        cookie.setMaxAge((int) RefreshTokenServiceImpl.REFRESH_TOKEN_VALIDITY_SECONDS);
        response.addCookie(cookie);
    }
}
