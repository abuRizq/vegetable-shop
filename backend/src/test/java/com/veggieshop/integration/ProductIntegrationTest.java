package com.veggieshop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veggieshop.product.Product;
import com.veggieshop.product.ProductDto.*;
import com.veggieshop.product.ProductRepository;
import com.veggieshop.category.Category;
import com.veggieshop.category.CategoryRepository;
import com.veggieshop.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/products";
    private static final String ADMIN_EMAIL = "jane@example.com";
    private static final String USER_EMAIL = "john@example.com";
    private static final String PASSWORD = "password";
    private static final String TEST_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36";

    private String adminToken;
    private String userToken;
    private Long testCategoryId;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = loginAndGetToken(ADMIN_EMAIL, PASSWORD);
        userToken = loginAndGetToken(USER_EMAIL, PASSWORD);

        // استخدم تصنيف افتراضي أو أنشئ واحد إن لم يوجد
        testCategoryId = categoryRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase("Vegetables"))
                .findFirst()
                .orElseGet(() -> categoryRepository.save(
                        new Category() {{
                            setName("Vegetables");
                            setDescription("Default test category");
                        }}
                ))
                .getId();
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

    private Optional<Product> findProductByName(String name) {
        return productRepository.findAll().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    // =========== TESTS ===========

    @Test @Order(1)
    void createProduct_admin_success() throws Exception {
        var req = new ProductCreateRequest();
        req.setName("Integration Product");
        req.setDescription("For testing only");
        req.setPrice(BigDecimal.valueOf(10.5));
        req.setDiscount(BigDecimal.valueOf(0.5));
        req.setFeatured(true);
        req.setCategoryId(testCategoryId);
        req.setImageUrl("https://test.img/1.png");

        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearer(adminToken))
                        .header("User-Agent", TEST_USER_AGENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 201 Created")
                .isEqualTo(201);
        assertThat(findProductByName("Integration Product"))
                .withFailMessage("Product should be saved in DB")
                .isPresent();
    }

    @Test @Order(2)
    void createProduct_duplicateName() throws Exception {
        var req = new ProductCreateRequest();
        req.setName("Integration Product"); // نفس الاسم
        req.setDescription("Duplicate test");
        req.setPrice(BigDecimal.valueOf(10));
        req.setCategoryId(testCategoryId);

        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearer(adminToken))
                        .header("User-Agent", TEST_USER_AGENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 409 Conflict for duplicate name")
                .isEqualTo(409);
    }

    @Test @Order(3)
    void createProduct_validationError_blankName() throws Exception {
        var req = new ProductCreateRequest();
        req.setName("");
        req.setDescription("desc");
        req.setPrice(BigDecimal.valueOf(5));
        req.setCategoryId(testCategoryId);

        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearer(adminToken))
                        .header("User-Agent", TEST_USER_AGENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 400 Bad Request for blank name")
                .isEqualTo(400);
    }

    @Test @Order(4)
    void createProduct_validationError_missingFields() throws Exception {
        var req = new ProductCreateRequest();

        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearer(adminToken))
                        .header("User-Agent", TEST_USER_AGENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 400 Bad Request for missing fields")
                .isEqualTo(400);
    }

    @Test @Order(5)
    void createProduct_forbidden_forUser() throws Exception {
        var req = new ProductCreateRequest();
        req.setName("UserProduct");
        req.setDescription("User try");
        req.setPrice(BigDecimal.valueOf(20));
        req.setCategoryId(testCategoryId);

        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearer(userToken))
                        .header("User-Agent", TEST_USER_AGENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 403 Forbidden for non-admin")
                .isEqualTo(403);
    }

    @Test @Order(6)
    void getAllProducts_success() throws Exception {
        var res = mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearer(adminToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 200 OK")
                .isEqualTo(200);
        assertThat(res.getResponse().getContentAsString())
                .contains("Integration Product");
    }

    @Test @Order(7)
    void getProductById_success() throws Exception {
        Product prod = findProductByName("Integration Product").orElseThrow();
        var res = mockMvc.perform(get(BASE_URL + "/" + prod.getId())
                        .header("Authorization", bearer(adminToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 200 OK for existing product")
                .isEqualTo(200);
        assertThat(res.getResponse().getContentAsString())
                .contains("Integration Product");
    }

    @Test @Order(8)
    void getProductById_notFound() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/99999")
                        .header("Authorization", bearer(adminToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 404 Not Found")
                .isEqualTo(404);
    }

    @Test @Order(9)
    void getFeaturedProducts_success() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/featured")
                        .header("Authorization", bearer(userToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 200 OK")
                .isEqualTo(200);
    }

    @Test @Order(10)
    void getProductsByCategory_success() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/category/" + testCategoryId)
                        .header("Authorization", bearer(userToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 200 OK")
                .isEqualTo(200);
    }

    @Test @Order(11)
    void getProductsByCategory_notFound() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/category/99999")
                        .header("Authorization", bearer(userToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 404 Not Found for missing category")
                .isEqualTo(404);
    }

    @Test @Order(12)
    void searchProductsByName_success() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/search")
                        .header("Authorization", bearer(userToken))
                        .param("name", "Integration"))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 200 OK")
                .isEqualTo(200);
        assertThat(res.getResponse().getContentAsString())
                .contains("Integration Product");
    }

    @Test @Order(13)
    void searchProductsByName_blankParam() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/search")
                        .header("Authorization", bearer(userToken))
                        .param("name", ""))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 400 Bad Request for blank param")
                .isEqualTo(400);
    }

    @Test @Order(14)
    void filterProductsByPrice_success() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/filter")
                        .header("Authorization", bearer(userToken))
                        .param("min", "0")
                        .param("max", "50"))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 200 OK")
                .isEqualTo(200);
    }

    @Test @Order(15)
    void filterProductsByPrice_invalidRange() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/filter")
                        .header("Authorization", bearer(userToken))
                        .param("min", "-5")
                        .param("max", "100"))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 400 Bad Request for invalid min value")
                .isEqualTo(400);
    }

    @Test @Order(16)
    void updateProduct_success() throws Exception {
        Product prod = findProductByName("Integration Product").orElseThrow();
        var req = new ProductUpdateRequest();
        req.setName("Integration Product Updated");
        req.setDescription("updated!");
        req.setPrice(BigDecimal.valueOf(19.95));
        req.setDiscount(BigDecimal.ZERO);
        req.setFeatured(false);
        req.setCategoryId(testCategoryId);
        req.setImageUrl("https://test.img/2.png");

        var res = mockMvc.perform(put(BASE_URL + "/" + prod.getId())
                        .header("Authorization", bearer(adminToken))
                        .header("User-Agent", TEST_USER_AGENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 200 OK on update")
                .isEqualTo(200);
        assertThat(findProductByName("Integration Product Updated"))
                .withFailMessage("Product name should be updated")
                .isPresent();
    }

    @Test @Order(17)
    void updateProduct_notFound() throws Exception {
        var req = new ProductUpdateRequest();
        req.setName("Should Not Exist");
        req.setDescription("test");
        req.setPrice(BigDecimal.valueOf(5));
        req.setDiscount(BigDecimal.ZERO);
        req.setFeatured(false);
        req.setCategoryId(testCategoryId);
        req.setImageUrl("url");

        var res = mockMvc.perform(put(BASE_URL + "/99999")
                        .header("Authorization", bearer(adminToken))
                        .header("User-Agent", TEST_USER_AGENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 404 Not Found on update for missing id")
                .isEqualTo(404);
    }

    @Test @Order(18)
    void updateProduct_validationError() throws Exception {
        Product prod = productRepository.findAll().stream().findFirst().orElseThrow();
        var req = new ProductUpdateRequest();
        req.setName("");
        req.setDescription("desc");
        req.setPrice(BigDecimal.valueOf(1));
        req.setDiscount(BigDecimal.ZERO);
        req.setFeatured(false);
        req.setCategoryId(testCategoryId);
        req.setImageUrl("url");

        var res = mockMvc.perform(put(BASE_URL + "/" + prod.getId())
                        .header("Authorization", bearer(adminToken))
                        .header("User-Agent", TEST_USER_AGENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 400 Bad Request for invalid data on update")
                .isEqualTo(400);
    }

    @Test @Order(19)
    void updateProduct_forbidden_forUser() throws Exception {
        Product prod = productRepository.findAll().stream().findFirst().orElseThrow();
        var req = new ProductUpdateRequest();
        req.setName("UserUpdateTest");
        req.setDescription("desc");
        req.setPrice(BigDecimal.valueOf(9));
        req.setDiscount(BigDecimal.ZERO);
        req.setFeatured(false);
        req.setCategoryId(testCategoryId);
        req.setImageUrl("url");

        var res = mockMvc.perform(put(BASE_URL + "/" + prod.getId())
                        .header("Authorization", bearer(userToken))
                        .header("User-Agent", TEST_USER_AGENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 403 Forbidden on user update attempt")
                .isEqualTo(403);
    }

    @Test @Order(20)
    void deleteProduct_success() throws Exception {
        // Create a product to delete
        var req = new ProductCreateRequest();
        req.setName("To Be Deleted Product");
        req.setDescription("for delete");
        req.setPrice(BigDecimal.valueOf(5));
        req.setCategoryId(testCategoryId);
        req.setImageUrl("url");
        mockMvc.perform(post(BASE_URL)
                .header("Authorization", bearer(adminToken))
                .header("User-Agent", TEST_USER_AGENT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(req))).andReturn();

        Product prod = findProductByName("To Be Deleted Product").orElseThrow();

        var res = mockMvc.perform(delete(BASE_URL + "/" + prod.getId())
                        .header("Authorization", bearer(adminToken)))
                .andReturn();

        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 204 No Content on successful delete")
                .isEqualTo(204);
        assertThat(productRepository.findById(prod.getId()))
                .withFailMessage("Product should be deleted from DB")
                .isNotPresent();
    }

    @Test @Order(21)
    void deleteProduct_notFound() throws Exception {
        var res = mockMvc.perform(delete(BASE_URL + "/99999")
                        .header("Authorization", bearer(adminToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 404 Not Found on delete for missing id")
                .isEqualTo(404);
    }

    @Test @Order(22)
    void deleteProduct_forbidden_forUser() throws Exception {
        Product prod = productRepository.findAll().stream().findFirst().orElseThrow();

        var res = mockMvc.perform(delete(BASE_URL + "/" + prod.getId())
                        .header("Authorization", bearer(userToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus())
                .withFailMessage("Should return 403 Forbidden on user delete attempt")
                .isEqualTo(403);
    }

//    @Test @Order(23)
//    void deleteProduct_softDeleteWhenHasOrders() throws Exception {
//        // لن يعمل إلا إذا كان عندك علاقات وربط فعلي بجدول order_item
//        Product prodWithOrder = ...; // جهز منتج مربوط بـ order item
//        var res = mockMvc.perform(delete(BASE_URL + "/" + prodWithOrder.getId())
//                        .header("Authorization", bearer(adminToken)))
//                .andReturn();
//
//        assertThat(res.getResponse().getStatus()).isEqualTo(204);
//        Product softDeleted = productRepository.findById(prodWithOrder.getId()).orElse(null);
//        assertThat(softDeleted).isNotNull();
//        assertThat(softDeleted.isActive()).isFalse(); // يتأكد من soft delete
//    }

}
