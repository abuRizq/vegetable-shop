package com.veggieshop.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthEndpointsSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36";

    @Nested
    @DisplayName("Public endpoints")
    class PublicEndpoints {

        @Test
        @DisplayName("Should allow registration for new user")
        void shouldAllowRegister() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("User-Agent", USER_AGENT)
                            .content("{\"email\": \"newuser@example.com\", \"password\": \"pass1234\", \"name\": \"newuser\"}"))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should return 409 for duplicate registration")
        void shouldReturn409ForDuplicateRegister() throws Exception {
            // Register once
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("User-Agent", USER_AGENT)
                            .content("{\"email\": \"duplicate@example.com\", \"password\": \"pass1234\", \"name\": \"duplicate\"}"))
                    .andExpect(status().isCreated());
            // Try again (should fail)
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("User-Agent", USER_AGENT)
                            .content("{\"email\": \"duplicate@example.com\", \"password\": \"pass1234\", \"name\": \"duplicate\"}"))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 400 for invalid register request")
        void shouldReturn400ForInvalidRegister() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("User-Agent", USER_AGENT)
                            .content("{\"email\": \"bademail\", \"password\": \"\", \"name\": \"\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should allow login with correct credentials")
        void shouldAllowLogin() throws Exception {
            // Register user
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("User-Agent", USER_AGENT)
                            .content("{\"email\": \"login@example.com\", \"password\": \"pass1234\", \"name\": \"loginuser\"}"))
                    .andExpect(status().isCreated());
            // Login
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("User-Agent", USER_AGENT)
                            .content("{\"email\": \"login@example.com\", \"password\": \"pass1234\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 400 for bad login credentials")
        void shouldReturn400ForBadLogin() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("User-Agent", USER_AGENT)
                            .content("{\"email\": \"notfound@example.com\", \"password\": \"wrongpass\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should allow forgot password endpoint (always 204)")
        void shouldAllowForgotPassword() throws Exception {
            mockMvc.perform(post("/api/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("User-Agent", USER_AGENT)
                            .content("{\"email\": \"anyone@example.com\"}"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Protected endpoints")
    class ProtectedEndpoints {

        @Test
        @DisplayName("Should deny access to /api/users/me without authentication")
        void shouldDenyMeWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isUnauthorized());
        }

//        @Test
//        @DisplayName("Should allow /api/users/me with authentication")
//        @WithMockUser(username = "john@example.com", roles = {"USER"})
//        void shouldAllowMeWithAuth() throws Exception {
//            mockMvc.perform(get("/api/users/me"))
//                    .andExpect(status().isOk());
//        }

        @Test
        @DisplayName("Should return 400 when logout is called without refreshToken cookie")
        @WithMockUser(username = "john@example.com")
        void shouldReturn400IfNoRefreshToken() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Admin endpoints")
    class AdminEndpoints {

        @Test
        @DisplayName("Should deny access to /api/users for unauthenticated users")
        void shouldDenyAdminListForUnauth() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should deny access to /api/users for authenticated user without ADMIN role")
        @WithMockUser(username = "john@example.com", roles = {"USER"})
        void shouldDenyAdminListForNonAdmin() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should allow access to /api/users for ADMIN user")
        @WithMockUser(username = "jane@example.com", roles = {"ADMIN"})
        void shouldAllowAdminListForAdmin() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Misc endpoints")
    class MiscEndpoints {
        @Test
        @DisplayName("Should allow Swagger UI docs without authentication")
        void shouldAllowSwaggerDocs() throws Exception {
            mockMvc.perform(get("/swagger-ui/index.html"))
                    .andExpect(status().isOk());
        }
    }
}
