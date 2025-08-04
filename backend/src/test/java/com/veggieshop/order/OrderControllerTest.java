package com.veggieshop.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veggieshop.common.ApiResponse;
import com.veggieshop.security.CustomUserDetails;
import com.veggieshop.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.beans.factory.annotation.Autowired;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private OrderService orderService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;


    @Autowired
    private ObjectMapper objectMapper;

    private OrderDto.OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        orderResponse = new OrderDto.OrderResponse();
        orderResponse.setId(1L);
        orderResponse.setUserId(10L);
        orderResponse.setUserName("User1");
        orderResponse.setTotalPrice(BigDecimal.valueOf(20));
        orderResponse.setStatus("PENDING");
        orderResponse.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void createOrder_shouldReturn201() throws Exception {
        OrderDto.OrderCreateRequest req = new OrderDto.OrderCreateRequest();
        // افترض أنك تملأ عناصر الطلب حسب الحاجة

        when(orderService.create(anyLong(), any(OrderDto.OrderCreateRequest.class)))
                .thenReturn(orderResponse);

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(10L);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(() -> "user")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAllOrders_shouldReturn200() throws Exception {
        Page<OrderDto.OrderResponse> page = new PageImpl<>(List.of(orderResponse));
        when(orderService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(orderResponse.getId()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getOrderById_shouldReturn200() throws Exception {
        when(orderService.findById(1L)).thenReturn(orderResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateStatus_shouldReturn204() throws Exception {
        doNothing().when(orderService).updateStatus(1L, "SHIPPED");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/1/status")
                        .param("status", "SHIPPED"))
                .andExpect(status().isNoContent());
    }

    // أضف اختبارات GET orders by user/status وما شابه حسب حاجتك
}
