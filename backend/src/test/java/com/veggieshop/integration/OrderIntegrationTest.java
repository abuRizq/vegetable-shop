package com.veggieshop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veggieshop.order.Order;
import com.veggieshop.order.OrderDto.*;
import com.veggieshop.order.OrderItemDto;
import com.veggieshop.order.OrderRepository;
import com.veggieshop.product.Product;
import com.veggieshop.product.ProductRepository;
import com.veggieshop.category.Category;
import com.veggieshop.category.CategoryRepository;
import com.veggieshop.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/orders";
    private static final String ADMIN_EMAIL = "jane@example.com";
    private static final String USER_EMAIL = "john@example.com";
    private static final String PASSWORD = "password";
    private static final String TEST_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36";

    private String adminToken;
    private String userToken;
    private Long userId;
    private Long adminId;
    private Long testProductId;
    static Long createdOrderId;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = loginAndGetToken(ADMIN_EMAIL, PASSWORD);
        userToken = loginAndGetToken(USER_EMAIL, PASSWORD);
        userId = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(USER_EMAIL)).findFirst().orElseThrow().getId();
        adminId = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(ADMIN_EMAIL)).findFirst().orElseThrow().getId();

        // Setup a test category and product
        Long catId = categoryRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase("OrderTestCat")).findFirst()
                .orElseGet(() -> {
                    Category cat = new Category();
                    cat.setName("OrderTestCat");
                    cat.setDescription("Test category for orders");
                    return categoryRepository.save(cat);
                }).getId();

        testProductId = productRepository.findAll().stream()
                .filter(p -> p.getName().equalsIgnoreCase("OrderTestProduct")).findFirst()
                .orElseGet(() -> productRepository.save(Product.builder()
                        .name("OrderTestProduct")
                        .description("Order test product")
                        .price(BigDecimal.valueOf(55))
                        .discount(BigDecimal.ZERO)
                        .featured(false)
                        .soldCount(0L)
                        .active(true)
                        .category(categoryRepository.findById(catId).orElseThrow())
                        .imageUrl("https://img/order_test.png")
                        .build()
                )).getId();
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

    private Optional<Order> findOrderByUser(Long userId) {
        return orderRepository.findByUserId(userId, PageRequest.of(0, 1))
                .stream().findFirst();
    }

    // ========== TESTS ==========

    @org.junit.jupiter.api.Order(1)
    @Test
    void createOrder_success() throws Exception {
        OrderItemDto.OrderItemCreateRequest item = new OrderItemDto.OrderItemCreateRequest();
        item.setProductId(testProductId);
        item.setQuantity(2);

        OrderCreateRequest req = new OrderCreateRequest();
        req.setItems(List.of(item));

        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(201);

        // Extract order ID for use in other tests
        var body = res.getResponse().getContentAsString();
        var node = objectMapper.readTree(body);
        createdOrderId = node.at("/data/id").asLong();
        assertThat(createdOrderId).isNotNull();
    }

    @org.junit.jupiter.api.Order(2)
    @Test
    void createOrder_validationError() throws Exception {
        // No items in the request
        OrderCreateRequest req = new OrderCreateRequest();
        req.setItems(null);

        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(400);
    }

    @org.junit.jupiter.api.Order(3)
    @Test
    void createOrder_productNotFound() throws Exception {
        OrderItemDto.OrderItemCreateRequest item = new OrderItemDto.OrderItemCreateRequest();
        item.setProductId(99999L);
        item.setQuantity(1);
        OrderCreateRequest req = new OrderCreateRequest();
        req.setItems(List.of(item));

        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(404);
    }

    @org.junit.jupiter.api.Order(4)
    @Test
    void createOrder_forbiddenForAdmin() throws Exception {
        // ADMIN can't create order (USER only)
        OrderItemDto.OrderItemCreateRequest item = new OrderItemDto.OrderItemCreateRequest();
        item.setProductId(testProductId);
        item.setQuantity(1);
        OrderCreateRequest req = new OrderCreateRequest();
        req.setItems(List.of(item));

        var res = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(403);
    }

    @org.junit.jupiter.api.Order(5)
    @Test
    void getAllOrders_asAdmin() throws Exception {
        var res = mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearer(adminToken)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(200);
        assertThat(res.getResponse().getContentAsString()).contains("OrderTestProduct");
    }

    @org.junit.jupiter.api.Order(6)
    @Test
    void getAllOrders_forbiddenForUser() throws Exception {
        var res = mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearer(userToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(403);
    }

    @org.junit.jupiter.api.Order(7)
    @Test
    void getUserOrders_asOwner() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/user/" + userId)
                        .header("Authorization", bearer(userToken)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(200);
    }

    @org.junit.jupiter.api.Order(8)
    @Test
    void getUserOrders_asAdmin() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/user/" + userId)
                        .header("Authorization", bearer(adminToken)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(200);
    }

    @org.junit.jupiter.api.Order(9)
    @Test
    void getUserOrders_forbiddenForOtherUser() throws Exception {
        // لنفترض لدينا مستخدم آخر
        Long anotherUserId = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(userId) && !u.getId().equals(adminId))
                .map(u -> u.getId()).findFirst().orElse(null);
        if (anotherUserId == null) return;

        var res = mockMvc.perform(get(BASE_URL + "/user/" + anotherUserId)
                        .header("Authorization", bearer(userToken)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(403);
    }

    @org.junit.jupiter.api.Order(10)
    @Test
    void getOrdersByStatus_asAdmin() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/status/PENDING")
                        .header("Authorization", bearer(adminToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(200);
    }

    @org.junit.jupiter.api.Order(11)
    @Test
    void getOrdersByStatus_invalidStatus() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/status/INVALID_STATUS")
                        .header("Authorization", bearer(adminToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(400);
    }

    @org.junit.jupiter.api.Order(12)
    @Test
    void getOrdersByStatus_forbiddenForUser() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/status/PENDING")
                        .header("Authorization", bearer(userToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(403);
    }

    @org.junit.jupiter.api.Order(13)
    @Test
    void getOrdersByUserAndStatus_asOwner() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/user/" + userId + "/status/PENDING")
                        .header("Authorization", bearer(userToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(200);
    }

    @org.junit.jupiter.api.Order(14)
    @Test
    void getOrdersByUserAndStatus_invalidStatus() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/user/" + userId + "/status/INVALID")
                        .header("Authorization", bearer(userToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(400);
    }

    @org.junit.jupiter.api.Order(15)
    @Test
    void getOrdersByUserAndStatus_forbiddenForOtherUser() throws Exception {
        Long anotherUserId = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(userId) && !u.getId().equals(adminId))
                .map(u -> u.getId()).findFirst().orElse(null);
        if (anotherUserId == null) return;

        var res = mockMvc.perform(get(BASE_URL + "/user/" + anotherUserId + "/status/PENDING")
                        .header("Authorization", bearer(userToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(403);
    }

    @org.junit.jupiter.api.Order(16)
    @Test
    void getOrderById_asOwner() throws Exception {
        // يفترض تم إنشاؤه في test 1
        var res = mockMvc.perform(get(BASE_URL + "/" + createdOrderId)
                        .header("Authorization", bearer(userToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(200);
    }

    @org.junit.jupiter.api.Order(17)
    @Test
    void getOrderById_asAdmin() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/" + createdOrderId)
                        .header("Authorization", bearer(adminToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(200);
    }

    @org.junit.jupiter.api.Order(18)
    @Test
    void getOrderById_notFound() throws Exception {
        var res = mockMvc.perform(get(BASE_URL + "/99999")
                        .header("Authorization", bearer(adminToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(404);
    }

    @org.junit.jupiter.api.Order(19)
    @Test
    void getOrderById_forbiddenForOtherUser() throws Exception {
        Long anotherUserId = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(userId) && !u.getId().equals(adminId))
                .map(u -> u.getId()).findFirst().orElse(null);
        if (anotherUserId == null) return;

        Optional<Order> anotherOrder = orderRepository.findByUserId(anotherUserId, PageRequest.of(0, 1))
                .stream().findFirst();
        if (anotherOrder.isEmpty()) return;

        var res = mockMvc.perform(get(BASE_URL + "/" + anotherOrder.get().getId())
                        .header("Authorization", bearer(userToken)))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(403);
    }

    @org.junit.jupiter.api.Order(20)
    @Test
    void updateOrderStatus_asAdmin_success() throws Exception {
        var res = mockMvc.perform(put(BASE_URL + "/" + createdOrderId + "/status")
                        .header("Authorization", bearer(adminToken))
                        .param("status", "PAID"))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(204);

        // تحقق فعلي من تحديث الحالة
        var updated = orderRepository.findById(createdOrderId).orElseThrow();
        assertThat(updated.getStatus().name()).isEqualTo("PAID");
    }

    @org.junit.jupiter.api.Order(21)
    @Test
    void updateOrderStatus_invalidStatus() throws Exception {
        var res = mockMvc.perform(put(BASE_URL + "/" + createdOrderId + "/status")
                        .header("Authorization", bearer(adminToken))
                        .param("status", "INVALID"))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(400);
    }

    @org.junit.jupiter.api.Order(22)
    @Test
    void updateOrderStatus_notFound() throws Exception {
        var res = mockMvc.perform(put(BASE_URL + "/99999/status")
                        .header("Authorization", bearer(adminToken))
                        .param("status", "PAID"))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(404);
    }

    @org.junit.jupiter.api.Order(23)
    @Test
    void updateOrderStatus_forbiddenForUser() throws Exception {
        var res = mockMvc.perform(put(BASE_URL + "/" + createdOrderId + "/status")
                        .header("Authorization", bearer(userToken))
                        .param("status", "PAID"))
                .andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(403);
    }
}
