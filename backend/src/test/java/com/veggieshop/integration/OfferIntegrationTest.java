package com.veggieshop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veggieshop.offer.Offer;
import com.veggieshop.offer.OfferDto.OfferCreateRequest;
import com.veggieshop.offer.OfferRepository;
import com.veggieshop.product.Product;
import com.veggieshop.product.ProductRepository;
import com.veggieshop.category.Category;
import com.veggieshop.category.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OfferIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private OfferRepository offerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ObjectMapper objectMapper;

    // Test constants
    private static final String BASE_URL = "/api/offers";
    private static final String ADMIN_EMAIL = "jane@example.com";
    private static final String USER_EMAIL = "john@example.com";
    private static final String PASSWORD = "password";
    private static final String TEST_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36";

    private String adminToken;
    private String userToken;
    private Long testProductId;
    static Long testOfferId;

    // ==== Setup before each test ====
    @BeforeEach
    void setUp() throws Exception {
        adminToken = authenticate(ADMIN_EMAIL, PASSWORD);
        userToken = authenticate(USER_EMAIL, PASSWORD);

        // Ensure a test category exists
        Long testCategoryId = categoryRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase("Vegetables"))
                .findFirst()
                .orElseGet(() -> {
                    Category cat = new Category();
                    cat.setName("Vegetables");
                    cat.setDescription("Default test category");
                    return categoryRepository.save(cat);
                }).getId();

        // Ensure a test product exists
        testProductId = productRepository.findAll().stream()
                .filter(p -> p.getName().equalsIgnoreCase("OfferTestProduct"))
                .findFirst()
                .orElseGet(() -> productRepository.save(Product.builder()
                        .name("OfferTestProduct")
                        .description("For Offer Integration Testing")
                        .price(BigDecimal.valueOf(50))
                        .discount(BigDecimal.ZERO)
                        .featured(false)
                        .soldCount(0L)
                        .active(true)
                        .category(categoryRepository.findById(testCategoryId).orElseThrow())
                        .imageUrl("https://img/offer_test.png")
                        .build()))
                .getId();
    }

    // ==== Utility Methods ====
    private String authenticate(String email, String password) throws Exception {
        var loginJson = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        var res = mockMvc.perform(post("/api/auth/login")
                        .header("User-Agent", TEST_USER_AGENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
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

    private String asJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private Optional<Offer> findOfferByDiscount(BigDecimal discount) {
        return offerRepository.findAll().stream()
                .filter(o -> o.getDiscount().compareTo(discount) == 0)
                .findFirst();
    }

    // =========== Tests ===========

    @Test @Order(1)
    void createOffer_admin_success() throws Exception {
        var req = new OfferCreateRequest();
        req.setProductId(testProductId);
        req.setDiscount(BigDecimal.valueOf(15));
        req.setStartDate(LocalDate.now().minusDays(1));
        req.setEndDate(LocalDate.now().plusDays(5));

        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearer(adminToken))
                        .header("User-Agent", TEST_USER_AGENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(201);
        var node = objectMapper.readTree(res.getResponse().getContentAsString());
        testOfferId = node.at("/data/id").asLong();
        assertThat(testOfferId).isNotNull();
    }

    @Test @Order(2)
    void createOffer_validationError_missingFields() throws Exception {
        var req = new OfferCreateRequest(); // كل القيم null
        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(400);
    }

    @Test @Order(3)
    void createOffer_productNotFound() throws Exception {
        var req = new OfferCreateRequest();
        req.setProductId(99999L);
        req.setDiscount(BigDecimal.valueOf(20));
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now().plusDays(5));

        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(404);
    }

    @Test @Order(4)
    void createOffer_forbidden_forUser() throws Exception {
        var req = new OfferCreateRequest();
        req.setProductId(testProductId);
        req.setDiscount(BigDecimal.valueOf(25));
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now().plusDays(2));

        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(403);
    }

    @Test @Order(5)
    void getAllOffers_paged_success() throws Exception {
        var res = mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearer(adminToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(200);
        assertThat(res.getResponse().getContentAsString()).contains("OfferTestProduct");
    }

    @Test @Order(6)
    void getOfferById_success() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/" + testOfferId)
                        .header("Authorization", bearer(userToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(200);
        assertThat(res.getResponse().getContentAsString()).contains("OfferTestProduct");
    }

    @Test @Order(7)
    void getOfferById_notFound() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/99999")
                        .header("Authorization", bearer(userToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(404);
    }

    @Test @Order(8)
    void getOfferById_invalidId() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/0")
                        .header("Authorization", bearer(userToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(400);
    }

    @Test @Order(9)
    void getOffersByProduct_success() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/product/" + testProductId)
                        .header("Authorization", bearer(userToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(200);
    }

    @Test @Order(10)
    void getOffersByProduct_notFound() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/product/99999")
                        .header("Authorization", bearer(userToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(200); // empty list (best practice!)
    }

    @Test @Order(11)
    void getOffersByProduct_invalidId() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/product/0")
                        .header("Authorization", bearer(userToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(400);
    }

    @Test @Order(12)
    void getActiveOffers_success() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/active")
                        .header("Authorization", bearer(userToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(200);
    }

    @Test @Order(13)
    void deleteOffer_success() throws Exception {
        // Create a test offer to delete it
        var req = new OfferCreateRequest();
        req.setProductId(testProductId);
        req.setDiscount(BigDecimal.valueOf(77));
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now().plusDays(2));
        mockMvc.perform(post(BASE_URL)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(req))).andReturn();

        Offer offer = findOfferByDiscount(BigDecimal.valueOf(77)).orElseThrow();

        var res = mockMvc.perform(delete(BASE_URL + "/" + offer.getId())
                        .header("Authorization", bearer(adminToken)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(204);
        assertThat(offerRepository.findById(offer.getId())).isNotPresent();
    }

    @Test @Order(14)
    void deleteOffer_notFound() throws Exception {
        var res = mockMvc.perform(delete(BASE_URL + "/99999")
                        .header("Authorization", bearer(adminToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(404);
    }

    @Test @Order(15)
    void deleteOffer_forbidden_forUser() throws Exception {
        Offer offer = offerRepository.findAll().stream().findFirst().orElseThrow();

        var res = mockMvc.perform(delete(BASE_URL + "/" + offer.getId())
                        .header("Authorization", bearer(userToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(403);
    }

}
