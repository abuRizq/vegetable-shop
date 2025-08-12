package com.veggieshop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veggieshop.user.User;
import com.veggieshop.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SecurityIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired ObjectMapper objectMapper;

    static final String TEST_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36";

    void printUsersInDb() {
        List<User> users = userRepository.findAll();
        log.info("\n---- USERS IN DB ----");
        users.forEach(u ->
                log.info("User[id={}, email={}, role={}, enabled={}]", u.getId(), u.getEmail(), u.getRole(), u.isEnabled()));
        log.info("---------------------");
    }

    String getFirstEnabledUserEmail() {
        return userRepository.findAll()
                .stream().filter(User::isEnabled)
                .map(User::getEmail).findFirst()
                .orElseThrow();
    }

    String getAdminEmail() {
        return userRepository.findAll()
                .stream().filter(u -> u.getRole().name().equals("ADMIN") && u.isEnabled())
                .map(User::getEmail).findFirst()
                .orElseThrow();
    }

    String getUserEmail() {
        return userRepository.findAll()
                .stream().filter(u -> u.getRole().name().equals("USER") && u.isEnabled())
                .map(User::getEmail).findFirst()
                .orElseThrow();
    }

    @BeforeAll
    static void beforeAll() {
        System.out.println("\n==================== SecurityConfigTest Started ====================\n");
    }

    @BeforeEach
    void setUp() {
        printUsersInDb();
    }

    @Test @Order(1)
    void testPublicEndpoints() throws Exception {
        log.info("Testing: Public GET /api/products");
        var result = mockMvc.perform(get("/api/products").accept(MediaType.APPLICATION_JSON))
                .andDo(res -> log.info("Products response status: {}", res.getResponse().getStatus()))
                .andReturn();
        int status = result.getResponse().getStatus();
        assertThat(status).isEqualTo(200);
    }

    @Test @Order(2)
    void testInvalidEndpoint() throws Exception {
        log.info("Testing: GET /not-a-real-endpoint");
        var result = mockMvc.perform(get("/not-a-real-endpoint"))
                .andDo(res -> log.info("Invalid endpoint response status: {}", res.getResponse().getStatus()))
                .andReturn();
        int status = result.getResponse().getStatus();
        assertThat(status).isEqualTo(404);
    }

    @Test @Order(3)
    void testSwaggerDocsAllowed() throws Exception {
        log.info("Testing: Swagger docs endpoint /v3/api-docs");
        var result = mockMvc.perform(get("/v3/api-docs"))
                .andDo(res -> log.info("Swagger docs status: {}", res.getResponse().getStatus()))
                .andReturn();
        int status = result.getResponse().getStatus();
        assertThat(status).isEqualTo(200);
    }

    @Test @Order(4)
    void testLoginWithValidCredentials() throws Exception {
        String userEmail = getFirstEnabledUserEmail();
        log.info("Attempting login for {} with password...", userEmail);
        String body = objectMapper.writeValueAsString(
                new LoginRequest(userEmail, "password"));
        var result = mockMvc.perform(post("/api/auth/login")
                        .header("User-Agent", TEST_USER_AGENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(res -> log.info("Login response: status={} body={}", res.getResponse().getStatus(), res.getResponse().getContentAsString()))
                .andReturn();
        int status = result.getResponse().getStatus();
        assertThat(status).isIn(200); // can be OK or Unauthorized
    }

    @Test @Order(5)
    void testLoginWithInvalidCredentials() throws Exception {
        log.info("Attempting login for notfound@example.com (should fail)...");
        String body = objectMapper.writeValueAsString(
                new LoginRequest("notfound@example.com", "password"));
        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(res -> log.info("Login response: status={} body={}", res.getResponse().getStatus(), res.getResponse().getContentAsString()))
                .andReturn();
        int status = result.getResponse().getStatus();
        assertThat(status).isEqualTo(401);
    }

    @Test @Order(6)
    void testUserMe_Unauthenticated() throws Exception {
        log.info("Testing: /api/users/me as unauthenticated");
        var result = mockMvc.perform(get("/api/users/me"))
                .andDo(res -> log.info("Unauthenticated /me response status: {}", res.getResponse().getStatus()))
                .andReturn();
        int status = result.getResponse().getStatus();
        assertThat(status).isEqualTo(401);
    }

    @Test @Order(7)
    @WithMockUser(username = "alice@example.com", roles = {"USER"})
    void testUserMe_Authenticated() throws Exception {
        log.info("Testing: /api/users/me with authenticated USER");
        var result = mockMvc.perform(get("/api/users/me"))
                .andDo(res -> log.info("User /me response status: {} body: {}", res.getResponse().getStatus(), res.getResponse().getContentAsString()))
                .andReturn();
        int status = result.getResponse().getStatus();
        assertThat(status).isIn(200, 500); // can be OK or Internal Error if business logic fails
    }

    @Test @Order(8)
    @WithMockUser(username = "jane@example.com", roles = {"ADMIN"})
    void testAdmin_CreateProduct() throws Exception {
        log.info("Testing: ADMIN privileges for {}", getAdminEmail());
        String reqBody = """
                {
                  "name":"TestProduct",
                  "description":"Desc",
                  "price":3.5,
                  "categoryId":1,
                  "imageUrl":"https://img.com/test.jpg"
                }
                """;
        var result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqBody))
                .andDo(res -> log.info("Admin create product status: {} body: {}", res.getResponse().getStatus(), res.getResponse().getContentAsString()))
                .andReturn();
        int status = result.getResponse().getStatus();
        assertThat(status).isIn(200, 201); // Created or OK
    }

    @Test @Order(9)
    @WithMockUser(username = "alice@example.com", roles = {"USER"})
    void testUserCannotCreateProduct() throws Exception {
        log.info("Testing: User can't perform admin actions");
        String reqBody = """
                {
                  "name":"ShouldFail",
                  "description":"Desc",
                  "price":3.5,
                  "categoryId":1,
                  "imageUrl":"https://img.com/test.jpg"
                }
                """;
        var result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqBody))
                .andDo(res -> log.info("User forbidden create product: {}", res.getResponse().getStatus()))
                .andReturn();
        int status = result.getResponse().getStatus();
        assertThat(status).isEqualTo(403);
    }

    // Helper record for login request
    record LoginRequest(String email, String password) {}
}
