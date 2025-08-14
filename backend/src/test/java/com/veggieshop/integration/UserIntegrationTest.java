package com.veggieshop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veggieshop.user.User;
import com.veggieshop.user.UserDto.*;
import com.veggieshop.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String BASE_URL = "/api/users";
    private static final String TEST_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36";

    private String accessToken;

    private Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Ensure Jane user (admin) exists for authentication.
     * Uses password: 'password'
     */
    private void ensureJaneUser() {
        String email = "jane@example.com";
        String encoded = passwordEncoder.encode("password");
        User jane = userRepository.findByEmail(email).orElse(null);

        if (jane == null) {
            jane = new User();
            jane.setName("Jane Smith");
            jane.setEmail(email);
            jane.setPassword(encoded);
            jane.setRole(User.Role.ADMIN);
            jane.setEnabled(true);
            jane.setCreatedAt(Instant.now());
        } else {
            jane.setPassword(encoded);
            jane.setName("Jane Smith");
            jane.setRole(User.Role.ADMIN);
            jane.setEnabled(true);
        }
        userRepository.save(jane);
    }

    /**
     * Setup: authenticate as Jane before every test and fetch JWT token.
     */
    @BeforeEach
    void setUp() throws Exception {
        ensureJaneUser();

        var loginRequest = """
                {
                  "email": "jane@example.com",
                  "password": "password"
                }
                """;
        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", TEST_USER_AGENT)
                        .content(loginRequest))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        var json = objectMapper.readTree(body);
        if (!json.has("data") || !json.get("data").has("token")) {
            throw new IllegalStateException("Failed to login and get JWT token. Response: " + body);
        }
        this.accessToken = json.get("data").get("token").asText();
    }

    private String asJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private String bearerToken() {
        return "Bearer " + accessToken;
    }

    // --- Tests ---

    @Test @Order(1)
    void createUser_success() throws Exception {
        log.info("Test: Create user with valid data");
        var req = new UserCreateRequest();
        req.setName("Integration User");
        req.setEmail("integration@example.com");
        req.setPassword("strongpassword");

        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(201);
        assertThat(findUserByEmail("integration@example.com")).isPresent();
        log.info("User created with status: {}", res.getResponse().getStatus());
    }

    @Test @Order(2)
    void createUser_emailConflict() throws Exception {
        log.info("Test: Create user with existing email");
        var req = new UserCreateRequest();
        req.setName("Duplicate User");
        req.setEmail("john@example.com");
        req.setPassword("password123");

        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(409);
        log.info("Email conflict: {}", res.getResponse().getContentAsString());
    }

    @Test @Order(3)
    void createUser_validationError() throws Exception {
        log.info("Test: Create user with invalid data");
        var req = new UserCreateRequest();
        req.setName("");
        req.setEmail("bad-email-format");
        req.setPassword("123");

        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(400);
        log.info("Validation error: {}", res.getResponse().getContentAsString());
    }

    @Test @Order(4)
    void getUserById_success() throws Exception {
        log.info("Test: Get existing user by id");
        Long userId = findUserByEmail("john@example.com").orElseThrow().getId();

        var res = mockMvc.perform(get(BASE_URL + "/" + userId)
                        .header("Authorization", bearerToken()))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(200);
        assertThat(res.getResponse().getContentAsString()).contains("john@example.com");
        log.info("Get user by id result: {}", res.getResponse().getContentAsString());
    }

    @Test @Order(5)
    void getUserById_notFound() throws Exception {
        log.info("Test: Get user by non-existent id");
        var res = mockMvc.perform(get(BASE_URL + "/99999")
                        .header("Authorization", bearerToken()))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(404);
        log.info("Not found error: {}", res.getResponse().getContentAsString());
    }

    @Test @Order(6)
    void getAllUsers_success() throws Exception {
        log.info("Test: Get all users");
        var res = mockMvc.perform(get(BASE_URL + "?page=0&size=10")
                        .header("Authorization", bearerToken()))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(200);
        assertThat(res.getResponse().getContentAsString()).contains("john@example.com");
        assertThat(res.getResponse().getContentAsString()).contains("jane@example.com");
        log.info("Get all users: {}", res.getResponse().getContentAsString());
    }

    @Test @Order(7)
    void searchUsers_success() throws Exception {
        log.info("Test: Search users by name/email");
        var res = mockMvc.perform(get(BASE_URL + "?q=john")
                        .header("Authorization", bearerToken()))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(200);
        assertThat(res.getResponse().getContentAsString()).contains("john@example.com");
        log.info("Search users result: {}", res.getResponse().getContentAsString());
    }

    @Test @Order(8)
    void updateUser_success() throws Exception {
        log.info("Test: Update user with valid data");
        User user = findUserByEmail("john@example.com").orElseThrow();
        var req = new UserUpdateRequest();
        req.setName("Updated John");
        req.setEmail("john.updated@example.com");

        var res = mockMvc.perform(put(BASE_URL + "/" + user.getId())
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(200);
        assertThat(findUserByEmail("john.updated@example.com")).isPresent();
        log.info("User updated: {}", res.getResponse().getContentAsString());
    }

    @Test @Order(9)
    void updateUser_notFound() throws Exception {
        log.info("Test: Update user not found");
        var req = new UserUpdateRequest();
        req.setName("Nobody");
        req.setEmail("nobody@example.com");

        var res = mockMvc.perform(put(BASE_URL + "/99999")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(404);
        log.info("Not found on update: {}", res.getResponse().getContentAsString());
    }

    @Test @Order(10)
    void updateUser_validationError() throws Exception {
        log.info("Test: Update user with invalid data");
        User user = userRepository.findAll().get(0);
        var req = new UserUpdateRequest();
        req.setName("");
        req.setEmail("bad-email");

        var res = mockMvc.perform(put(BASE_URL + "/" + user.getId())
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(400);
        log.info("Validation error on update: {}", res.getResponse().getContentAsString());
    }

    @Test @Order(11)
    void deleteUser_success() throws Exception {
        log.info("Test: Delete user by id");
        User toDelete = findUserByEmail("alice@example.com").orElseThrow();
        var res = mockMvc.perform(delete(BASE_URL + "/" + toDelete.getId())
                        .header("Authorization", bearerToken()))
                .andReturn();

        int status = res.getResponse().getStatus();
        assertThat(status == 204 || status == 409)
                .withFailMessage("Expected 204 (No Content) or 409 (Conflict) but was: " + status)
                .isTrue();

        if (status == 204) {
            assertThat(userRepository.findById(toDelete.getId())).isNotPresent();
        }
        log.info("User delete status: {}", status);
    }

    @Test @Order(12)
    void deleteUser_notFound() throws Exception {
        log.info("Test: Delete user not found");
        var res = mockMvc.perform(delete(BASE_URL + "/99999")
                        .header("Authorization", bearerToken()))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(404);
        log.info("Not found on delete: {}", res.getResponse().getContentAsString());
    }

    @Test @Order(13)
    void changePassword_success() throws Exception {
        log.info("Test: Change password success");
        var req = new PasswordChangeRequest();
        req.setOldPassword("password");
        req.setNewPassword("newSecret123");

        var res = mockMvc.perform(put(BASE_URL + "/me/password")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(204);
        log.info("Password changed: {}", res.getResponse().getStatus());
    }

    @Test @Order(14)
    void changePassword_wrongOldPassword() throws Exception {
        log.info("Test: Change password wrong old password");
        var req = new PasswordChangeRequest();
        req.setOldPassword("wrongOld");
        req.setNewPassword("anotherSecret123");

        var res = mockMvc.perform(put(BASE_URL + "/me/password")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(400);
        log.info("Wrong old password: {}", res.getResponse().getContentAsString());
    }

    @Test @Order(15)
    void changePassword_notFound() throws Exception {
        log.info("Test: Change password user not found");
        var req = new PasswordChangeRequest();
        req.setOldPassword("any");
        req.setNewPassword("newSecret");

        var res = mockMvc.perform(post(BASE_URL + "/99999/change-password")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(404);
        log.info("Not found on change password: {}", res.getResponse().getContentAsString());
    }

    @Test @Order(16)
    void changePassword_validationError() throws Exception {
        log.info("Test: Change password validation error");
        var req = new PasswordChangeRequest();
        req.setOldPassword("");
        req.setNewPassword("12");

        var res = mockMvc.perform(put(BASE_URL + "/me/password")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(400);
        log.info("Validation error on change password: {}", res.getResponse().getContentAsString());
    }

    @Test @Order(17)
    void changeRole_success() throws Exception {
        log.info("Test: Change role success");
        User user = findUserByEmail("john.updated@example.com").orElseThrow();
        var req = new RoleChangeRequest();
        req.setRole(User.Role.ADMIN);

        var res = mockMvc.perform(put(BASE_URL + "/" + user.getId() + "/role")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(200);
        log.info("Role changed: {}", res.getResponse().getContentAsString());
    }

    @Test @Order(18)
    void changeRole_notFound() throws Exception {
        log.info("Test: Change role user not found");
        var req = new RoleChangeRequest();
        req.setRole(User.Role.USER);

        var res = mockMvc.perform(put(BASE_URL + "/99999/role")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(404);
        log.info("Not found on change role: {}", res.getResponse().getContentAsString());
    }

    @Test @Order(19)
    void changeRole_validationError() throws Exception {
        log.info("Test: Change role validation error");
        User user = userRepository.findAll().get(0);
        var req = new RoleChangeRequest();
        req.setRole(null); // invalid

        var res = mockMvc.perform(put(BASE_URL + "/" + user.getId() + "/role")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(400);
        log.info("Validation error on change role: {}", res.getResponse().getContentAsString());
    }
}
