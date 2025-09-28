package com.veggieshop.auth;

import com.veggieshop.common.ApiResponse;
import com.veggieshop.common.ApiError;
import com.veggieshop.common.ApiResponseUtil;
import com.veggieshop.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Auth Controller", description = "APIs for authentication and session management")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SessionMapper sessionMapper;

    // ========== REGISTER ==========
    @Operation(
            summary = "Register a new user",
            description = "Registers a new user. Prevents duplicate emails. Returns the created user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate email",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AuthDto.RegisterRequest.class))
            )
            @RequestBody @Valid AuthDto.RegisterRequest request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        String deviceInfo = httpServletRequest.getHeader("User-Agent");
        AuthDto.AuthResponse response = authService.register(request, deviceInfo, httpServletResponse);
        return ApiResponseUtil.created(response);
    }

    // ========== LOGIN ==========
    @Operation(
            summary = "User login",
            description = "Login with email and password. Returns a JWT access token and user data. Sets refresh token in HttpOnly cookie."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or bad credentials",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AuthDto.AuthRequest.class))
            )
            @RequestBody @Valid AuthDto.AuthRequest request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        String deviceInfo = httpServletRequest.getHeader("User-Agent");
        AuthDto.AuthResponse response = authService.login(request, deviceInfo, httpServletResponse);
        return ApiResponseUtil.ok(response);
    }

    // ========== REFRESH ==========
    @Operation(
            summary = "Refresh JWT access token",
            description = "Rotates and refreshes JWT using the refresh token from HttpOnly cookie."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired refresh token",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthDto.RefreshResponse>> refresh(
            @Parameter(hidden = true)
            @CookieValue(name = "refreshToken") String refreshToken,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        String deviceInfo = httpServletRequest.getHeader("User-Agent");
        AuthDto.RefreshResponse response = authService.refresh(refreshToken, deviceInfo, httpServletResponse);
        return ApiResponseUtil.ok(response);
    }

    // ========== LOGOUT ==========
    @Operation(
            summary = "Logout",
            description = "Logs out the current session by revoking the refresh token (cookie-based)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Logged out",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(hidden = true)
            @CookieValue(name = "refreshToken") String refreshToken,
            HttpServletResponse httpServletResponse
    ) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        httpServletResponse.addCookie(cookie);

        return ApiResponseUtil.noContent();
    }

    // ========== FORGOT PASSWORD ==========
    @Operation(
            summary = "Forgot password",
            description = "Send password reset link to user's email. Always responds successfully to avoid email enumeration."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "If the email exists, a reset link will be sent."
            )
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Forgot password request (email only)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AuthDto.ForgotPasswordRequest.class))
            )
            @RequestBody @Valid AuthDto.ForgotPasswordRequest request,
            HttpServletRequest httpRequest // Optional: for IP/device info logging
    ) {
        String requestIp = httpRequest.getRemoteAddr();
        authService.sendResetPasswordLink(request, requestIp);
        // Always return a generic message/status (do not reveal if email exists)
        return ResponseEntity.noContent().build();
    }

    // ========== RESET PASSWORD ==========
    @Operation(
            summary = "Reset password",
            description = "Reset user's password using the token received in email."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Password reset successful",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired token",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Reset password data (token and new password)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AuthDto.ResetPasswordRequest.class))
            )
            @RequestBody @Valid AuthDto.ResetPasswordRequest request
    ) {
        authService.resetPassword(request);
        return ApiResponseUtil.noContent();
    }

    // ========== GET ALL USER SESSIONS ==========
    @Operation(
            summary = "Get all active sessions for current user",
            description = "Lists all active sessions (devices) for the authenticated user, including current session indicator."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "List of sessions",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))
            )
    })
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<SessionsResponse>> getUserSessions(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(hidden = true)
            @CookieValue(name = "refreshToken", required = false) String currentRefreshToken
    ) {
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        var tokens = refreshTokenRepository.findByUser(user);
        var sessions = tokens.stream().map(sessionMapper::toSessionDto).toList();

        Long currentSessionId = tokens.stream()
                .filter(token -> token.getToken().equals(currentRefreshToken))
                .map(RefreshToken::getId)
                .findFirst()
                .orElse(null);

        SessionsResponse resp = new SessionsResponse(sessions, currentSessionId);
        return ApiResponseUtil.ok(resp);
    }

    // ========== REVOKE A SESSION (LOGOUT FROM DEVICE) ==========
    @Operation(
            summary = "Revoke a session (logout from device)",
            description = "Revokes a refresh token/session for a given device/session ID."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Session revoked",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/sessions/{id}/revoke")
    public ResponseEntity<ApiResponse<Void>> revokeSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Session ID to revoke", required = true)
            @PathVariable Long id
    ) {
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var token = refreshTokenRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        if (!token.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);

        return ApiResponseUtil.noContent();
    }
}
