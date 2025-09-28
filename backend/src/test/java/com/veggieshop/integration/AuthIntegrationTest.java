package com.veggieshop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veggieshop.auth.AuthDto;
import com.veggieshop.user.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.servlet.http.Cookie;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper objectMapper;

    static final String TEST_USER_AGENT = "JUnitTest/1.0";
    static final String TEST_USER_EMAIL = "testuser@veggie.com";
    static final String TEST_USER_PASSWORD = "testpass123";
    String jwtToken;
    Cookie refreshTokenCookie;
    Long testUserSessionId;

    @BeforeAll
    void setup() throws Exception {
        AuthDto.RegisterRequest registerRequest = new AuthDto.RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail(TEST_USER_EMAIL);
        registerRequest.setPassword(TEST_USER_PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", TEST_USER_AGENT)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }

    void loginAsTestUser() throws Exception {
        AuthDto.AuthRequest loginRequest = new AuthDto.AuthRequest();
        loginRequest.setEmail(TEST_USER_EMAIL);
        loginRequest.setPassword(TEST_USER_PASSWORD);

        MockHttpServletResponse loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", TEST_USER_AGENT)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andReturn().getResponse();

        Map<?, ?> parsed = objectMapper.readValue(loginResponse.getContentAsString(), Map.class);
        Map<?, ?> data = (Map<?, ?>) parsed.get("data");
        jwtToken = (String) data.get("token");
        refreshTokenCookie = loginResponse.getCookie("refreshToken");

        // Fetch first session ID for session-revoke tests
        MockHttpServletResponse sessionsResp = mockMvc.perform(get("/api/auth/sessions")
                        .cookie(refreshTokenCookie)
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("User-Agent", TEST_USER_AGENT))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        Map<?, ?> sData = objectMapper.readValue(sessionsResp.getContentAsString(), Map.class);
        Map<?, ?> dData = (Map<?, ?>) sData.get("data");
        List<?> sessions = (List<?>) dData.get("sessions");
        Map<?, ?> firstSession = (Map<?, ?>) sessions.get(0);
        testUserSessionId = Long.valueOf(firstSession.get("id").toString());
    }

    @Test
    void register_shouldFail_onDuplicateEmail() throws Exception {
        AuthDto.RegisterRequest registerRequest = new AuthDto.RegisterRequest();
        registerRequest.setName("Test User 2");
        registerRequest.setEmail(TEST_USER_EMAIL);
        registerRequest.setPassword("anotherpass123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", TEST_USER_AGENT)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void login_shouldSucceed_andSetRefreshCookie_andReturnJwt() throws Exception {
        loginAsTestUser();
        assertThat(jwtToken).isNotNull();
        assertThat(refreshTokenCookie).isNotNull();
    }

    @Test
    void login_shouldFail_withWrongPassword() throws Exception {
        AuthDto.AuthRequest loginRequest = new AuthDto.AuthRequest();
        loginRequest.setEmail(TEST_USER_EMAIL);
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", TEST_USER_AGENT)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void refreshToken_shouldSucceed_andRotateToken() throws Exception {
        loginAsTestUser();

        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshTokenCookie)
                        .header("User-Agent", TEST_USER_AGENT)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andReturn().getResponse();

        Map<?, ?> parsed = objectMapper.readValue(response.getContentAsString(), Map.class);
        Map<?, ?> data = (Map<?, ?>) parsed.get("data");
        assertThat(data.get("accessToken")).isNotNull();
        assertThat(data.get("userEmail")).isEqualTo(TEST_USER_EMAIL);
    }

    @Test
    void refreshToken_shouldFail_withInvalidToken() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", "invalid-token"))
                        .header("User-Agent", TEST_USER_AGENT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldClearCookie_andRevokeSession() throws Exception {
        loginAsTestUser();

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(refreshTokenCookie)
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("User-Agent", TEST_USER_AGENT))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refreshToken", 0));
    }

    @Test
    void forgotPassword_shouldReturn204_forAnyEmail() throws Exception {
        AuthDto.ForgotPasswordRequest request = new AuthDto.ForgotPasswordRequest();
        request.setEmail(TEST_USER_EMAIL);

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", TEST_USER_AGENT)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        request.setEmail("notfound@veggie.com");
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", TEST_USER_AGENT)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void resetPassword_shouldFail_forInvalidToken() throws Exception {
        AuthDto.ResetPasswordRequest resetRequest = new AuthDto.ResetPasswordRequest();
        resetRequest.setToken("badtoken");
        resetRequest.setNewPassword("newpass123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", TEST_USER_AGENT)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSessions_shouldReturnActiveSessions_forAuthenticatedUser() throws Exception {
        loginAsTestUser();

        mockMvc.perform(get("/api/auth/sessions")
                        .cookie(refreshTokenCookie)
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("User-Agent", TEST_USER_AGENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessions").isArray())
                .andExpect(jsonPath("$.data.currentSessionId").exists());
    }

    @Test
    void revokeSession_shouldSucceed_forOwnSession() throws Exception {
        loginAsTestUser();

        mockMvc.perform(post("/api/auth/sessions/" + testUserSessionId + "/revoke")
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("User-Agent", TEST_USER_AGENT))
                .andExpect(status().isNoContent());
    }

    @Test
    void revokeSession_shouldFail_forInvalidOrOtherUserSession() throws Exception {
        loginAsTestUser();

        // Non-existing sessionId
        mockMvc.perform(post("/api/auth/sessions/999999/revoke")
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("User-Agent", TEST_USER_AGENT))
                .andExpect(status().isNotFound());

        // Register and login as another user
        String otherEmail = "other@veggie.com";
        String otherPass = "pass56789";
        AuthDto.RegisterRequest otherUser = new AuthDto.RegisterRequest();
        otherUser.setName("Other User");
        otherUser.setEmail(otherEmail);
        otherUser.setPassword(otherPass);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", TEST_USER_AGENT)
                        .content(objectMapper.writeValueAsString(otherUser)))
                .andExpect(status().isCreated());

        AuthDto.AuthRequest loginReq = new AuthDto.AuthRequest();
        loginReq.setEmail(otherEmail);
        loginReq.setPassword(otherPass);
        MockHttpServletResponse otherLoginResp = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", TEST_USER_AGENT)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        String otherJwt = (String) ((Map<?, ?>) ((Map<?, ?>) objectMapper.readValue(otherLoginResp.getContentAsString(), Map.class)).get("data")).get("token");

        // Try to revoke the test user's session with the other user
        mockMvc.perform(post("/api/auth/sessions/" + testUserSessionId + "/revoke")
                        .header("Authorization", "Bearer " + otherJwt)
                        .header("User-Agent", TEST_USER_AGENT))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }
}
