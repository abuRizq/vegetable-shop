package com.veggieshop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veggieshop.category.Category;
import com.veggieshop.category.CategoryDto.*;
import com.veggieshop.category.CategoryRepository;
import com.veggieshop.product.ProductRepository;
import com.veggieshop.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CategoryIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/categories";
    private static final String ADMIN_EMAIL = "jane@example.com";
    private static final String USER_EMAIL = "john@example.com";
    private static final String PASSWORD = "password";
    private static final String TEST_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36";

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = loginAndGetToken(ADMIN_EMAIL, PASSWORD);
        userToken = loginAndGetToken(USER_EMAIL, PASSWORD);
    }


    private String loginAndGetToken(String email, String password) throws Exception {
        var loginReq = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
        var res = mockMvc.perform(post("/api/auth/login")
                        .header("User-Agent", TEST_USER_AGENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginReq))
                .andReturn();
        var body = res.getResponse().getContentAsString();
        var json = objectMapper.readTree(body);
        if (!json.has("data") || json.get("data").isNull() || !json.get("data").has("token")) {
            throw new IllegalStateException("Login failed: " + body);
        }
        return json.get("data").get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private Optional<Category> findCategoryByName(String name) {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    // ================= TESTS =================

    @Test @Order(1)
    void createCategory_admin_success() throws Exception {
        var req = new CategoryCreateRequest();
        req.setName("Integration Category");
        req.setDescription("For testing");

        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(201);
        assertThat(findCategoryByName("Integration Category")).isPresent();
    }

    @Test @Order(2)
    void createCategory_admin_duplicateName() throws Exception {
        var req = new CategoryCreateRequest();
        req.setName("Vegetables"); // مفترض أنها موجودة
        req.setDescription("Duplicate");

        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(409);
    }

    @Test @Order(3)
    void createCategory_admin_validationError() throws Exception {
        var req = new CategoryCreateRequest();
        req.setName(""); // NotBlank
        req.setDescription("desc");

        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(400);
    }

    @Test @Order(4)
    void createCategory_forbidden_forUser() throws Exception {
        var req = new CategoryCreateRequest();
        req.setName("User Not Allowed");
        req.setDescription("No rights");

        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(403);
    }

    @Test @Order(5)
    void getAllCategories_admin_success() throws Exception {
        var res = mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearer(adminToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(200);
        assertThat(res.getResponse().getContentAsString()).contains("Vegetables");
    }

    @Test @Order(6)
    void searchCategories_byName_success() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/search")
                        .header("Authorization", bearer(adminToken))
                        .param("name", "Veg"))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(200);
        assertThat(res.getResponse().getContentAsString()).contains("Vegetables");
    }

    @Test @Order(7)
    void getCategoryById_success() throws Exception {
        Category cat = findCategoryByName("Vegetables").orElseThrow();
        var res = mockMvc.perform(get(BASE_URL + "/" + cat.getId())
                        .header("Authorization", bearer(adminToken)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(200);
        assertThat(res.getResponse().getContentAsString()).contains("Vegetables");
    }

    @Test @Order(8)
    void getCategoryById_notFound() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/99999")
                        .header("Authorization", bearer(adminToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(404);
    }

    @Test @Order(9)
    void updateCategory_success() throws Exception {
        Category cat = findCategoryByName("Fruits").orElseThrow();
        var req = new CategoryUpdateRequest();
        req.setName("Fruits Updated");
        req.setDescription("Updated!");

        var res = mockMvc.perform(put(BASE_URL + "/" + cat.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(200);
        assertThat(findCategoryByName("Fruits Updated")).isPresent();
    }

    @Test @Order(10)
    void updateCategory_notFound() throws Exception {
        var req = new CategoryUpdateRequest();
        req.setName("Nonexistent");
        req.setDescription("desc");

        var res = mockMvc.perform(put(BASE_URL + "/99999")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(404);
    }

    @Test @Order(11)
    void updateCategory_validationError() throws Exception {
        Category cat = findCategoryByName("Herbs").orElseThrow();
        var req = new CategoryUpdateRequest();
        req.setName(""); // NotBlank
        req.setDescription("desc");

        var res = mockMvc.perform(put(BASE_URL + "/" + cat.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(400);
    }

    @Test @Order(12)
    void updateCategory_forbidden_forUser() throws Exception {
        Category cat = findCategoryByName("Herbs").orElseThrow();
        var req = new CategoryUpdateRequest();
        req.setName("UserUpdate");
        req.setDescription("try");

        var res = mockMvc.perform(put(BASE_URL + "/" + cat.getId())
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(403);
    }

    @Test @Order(13)
    void deleteCategory_success() throws Exception {
        var req = new CategoryCreateRequest();
        req.setName("To Be Deleted");
        req.setDescription("for delete");
        mockMvc.perform(post(BASE_URL)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(req))).andReturn();

        Category cat = findCategoryByName("To Be Deleted").orElseThrow();

        var res = mockMvc.perform(delete(BASE_URL + "/" + cat.getId())
                        .header("Authorization", bearer(adminToken)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(204);
        assertThat(categoryRepository.findById(cat.getId())).isNotPresent();
    }

    @Test @Order(14)
    void deleteCategory_notFound() throws Exception {
        var res = mockMvc.perform(delete(BASE_URL + "/99999")
                        .header("Authorization", bearer(adminToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(404);
    }

    @Test @Order(15)
    void deleteCategory_withProducts_badRequest() throws Exception {
        Category cat = findCategoryByName("Vegetables").orElseThrow();

        var res = mockMvc.perform(delete(BASE_URL + "/" + cat.getId())
                        .header("Authorization", bearer(adminToken)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(400);
    }

    @Test @Order(16)
    void deleteCategory_forbidden_forUser() throws Exception {
        Category cat = findCategoryByName("Herbs").orElseThrow();

        var res = mockMvc.perform(delete(BASE_URL + "/" + cat.getId())
                        .header("Authorization", bearer(userToken)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(403);
    }
}
