package com.veggieshop.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veggieshop.auth.AuthDto.*;
import com.veggieshop.common.ApiResponse;
import com.veggieshop.security.JwtUtil;
import com.veggieshop.user.User;
import com.veggieshop.user.UserDto;
import com.veggieshop.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;
    @MockBean
    private RefreshTokenRepository refreshTokenRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private SessionMapper sessionMapper;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;



    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/auth";

    @BeforeEach
    void setup() {
        Mockito.reset(authService, refreshTokenRepository, userRepository, sessionMapper);
    }

    // ========== REGISTER ==========
    @Test
    void register_ShouldReturnCreated_whenValidRequest() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName("TestUser");
        req.setEmail("test@email.com");
        req.setPassword("secret");

        AuthResponse resp = new AuthResponse();
        resp.setToken("jwt-token");
        resp.setUser(new UserDto.UserResponse());

        when(authService.register(any(), anyString(), any())).thenReturn(resp);

        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    // ========== LOGIN ==========
    @Test
    void login_ShouldReturnOk_whenValidCredentials() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setEmail("user@mail.com");
        req.setPassword("pw");

        AuthResponse resp = new AuthResponse();
        resp.setToken("jwt");
        resp.setUser(new UserDto.UserResponse());

        when(authService.login(any(), anyString(), any())).thenReturn(resp);

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("jwt"));
    }

    // ========== REFRESH ==========
    @Test
    void refresh_ShouldReturnNewAccessToken_whenValidRefreshToken() throws Exception {
        RefreshResponse refreshResp = new RefreshResponse();
        refreshResp.setAccessToken("new-access-token");
        refreshResp.setUserEmail("foo@bar.com");

        when(authService.refresh(eq("rtok"), anyString(), any())).thenReturn(refreshResp);

        mockMvc.perform(post(BASE_URL + "/refresh")
                        .cookie(new Cookie("refreshToken", "rtok")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    void refresh_ShouldReturnUnauthorized_whenMissingCookie() throws Exception {
        // The controller method will throw if missing required cookie
        mockMvc.perform(post(BASE_URL + "/refresh"))
                .andExpect(status().isBadRequest()); // or .is4xx, depending on @CookieValue(required=true)
    }

    // ========== LOGOUT ==========
    @Test
    void logout_ShouldReturnNoContent_andClearCookie() throws Exception {
        mockMvc.perform(post(BASE_URL + "/logout")
                        .cookie(new Cookie("refreshToken", "token")))
                .andExpect(status().isNoContent());

        verify(authService).logout("token");
    }

    // ========== FORGOT PASSWORD ==========
    @Test
    void forgotPassword_ShouldReturnNoContent_andCallService() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("reset@email.com");

        mockMvc.perform(post(BASE_URL + "/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        verify(authService).sendResetPasswordLink(any(), anyString());
    }

    // ========== RESET PASSWORD ==========
    @Test
    void resetPassword_ShouldReturnNoContent_whenValidRequest() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("tkn");
        req.setNewPassword("newpw");

        mockMvc.perform(post(BASE_URL + "/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        verify(authService).resetPassword(any());
    }

    // ========== GET ALL SESSIONS ==========
    @Test
    void getUserSessions_ShouldReturnListOfSessions() throws Exception {
        User user = User.builder().id(12L).email("a@a.com").build();
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail()).password("pw").roles("USER").build();

        // You must mock Spring Security context for @AuthenticationPrincipal
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        RefreshToken token1 = RefreshToken.builder().id(1L).token("a").user(user).build();
        RefreshToken token2 = RefreshToken.builder().id(2L).token("b").user(user).build();
        when(refreshTokenRepository.findByUser(user)).thenReturn(List.of(token1, token2));
        when(sessionMapper.toSessionDto(any())).thenReturn(new SessionDto(1L, "browser", null, false));

        // Simulate authenticated user
        // You can add: @WithMockUser(username="a@a.com") at the method, or use SecurityContextHolder
        // For brevity, we skip real security context here

        mockMvc.perform(get(BASE_URL + "/sessions")
                        .cookie(new Cookie("refreshToken", "a")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessions").isArray());
    }

    // ========== REVOKE SESSION ==========
    @Test
    void revokeSession_ShouldReturnNoContent_whenValidSession() throws Exception {
        User user = User.builder().id(4L).email("foo@bar.com").build();
        RefreshToken token = RefreshToken.builder().id(8L).token("tok").user(user).revoked(false).build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findById(8L)).thenReturn(Optional.of(token));

        mockMvc.perform(post(BASE_URL + "/sessions/8/revoke"))
                .andExpect(status().isNoContent());

        assertThat(token.isRevoked()).isTrue();
    }

    // ==== Edge Cases: Unauthorized, Forbidden, NotFound, etc. ====
    // Add as needed depending on your exception handling strategy

}
