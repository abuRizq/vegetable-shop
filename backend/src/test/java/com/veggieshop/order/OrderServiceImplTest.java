package com.veggieshop.order;

import com.veggieshop.exception.ResourceNotFoundException;
import com.veggieshop.offer.Offer;
import com.veggieshop.offer.OfferRepository;
import com.veggieshop.product.Product;
import com.veggieshop.product.ProductRepository;
import com.veggieshop.user.User;
import com.veggieshop.user.UserRepository;
import com.veggieshop.util.PriceCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private OfferRepository offerRepository;
    @Mock private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ============ CREATE =============
    @Test
    void shouldCreateOrderWithValidUserAndProduct() {
        Long userId = 1L, productId = 2L;
        User user = User.builder().id(userId).name("Test User").build();
        Product product = Product.builder().id(productId).name("Apple").price(BigDecimal.TEN).soldCount(0L).build();

        OrderItemDto.OrderItemCreateRequest itemReq = new OrderItemDto.OrderItemCreateRequest();
        itemReq.setProductId(productId);
        itemReq.setQuantity(2);
        OrderDto.OrderCreateRequest orderReq = new OrderDto.OrderCreateRequest();
        orderReq.setItems(List.of(itemReq));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(offerRepository.findByProductId(eq(productId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        try (MockedStatic<PriceCalculator> calculatorMock = mockStatic(PriceCalculator.class)) {
            calculatorMock.when(() ->
                            PriceCalculator.calculateFinalPrice(any(Product.class), anyList(), any(LocalDate.class)))
                    .thenReturn(BigDecimal.TEN);

            Order savedOrder = Order.builder()
                    .id(99L).user(user)
                    .status(Order.Status.PENDING)
                    .orderItems(new ArrayList<>())
                    .totalPrice(BigDecimal.valueOf(20))
                    .createdAt(LocalDateTime.now())
                    .build();

            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            OrderDto.OrderResponse orderResponse = new OrderDto.OrderResponse();
            orderResponse.setId(99L);
            orderResponse.setUserId(userId);
            orderResponse.setTotalPrice(BigDecimal.valueOf(20));
            when(orderMapper.toOrderResponse(savedOrder)).thenReturn(orderResponse);

            OrderDto.OrderResponse result = orderService.create(userId, orderReq);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(99L);
            verify(productRepository).save(argThat(p -> p.getSoldCount() == 2));
        }
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(42L)).thenReturn(Optional.empty());
        OrderDto.OrderCreateRequest req = new OrderDto.OrderCreateRequest();
        assertThatThrownBy(() -> orderService.create(42L, req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        Long userId = 1L, productId = 77L;
        User user = User.builder().id(userId).build();
        OrderItemDto.OrderItemCreateRequest itemReq = new OrderItemDto.OrderItemCreateRequest();
        itemReq.setProductId(productId);
        itemReq.setQuantity(1);
        OrderDto.OrderCreateRequest req = new OrderDto.OrderCreateRequest();
        req.setItems(List.of(itemReq));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(userId, req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    // ============ FIND BY ID ============
    @Test
    void shouldFindOrderById() {
        Order order = Order.builder().id(5L).build();
        OrderDto.OrderResponse resp = new OrderDto.OrderResponse();
        resp.setId(5L);

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(orderMapper.toOrderResponse(order)).thenReturn(resp);

        OrderDto.OrderResponse result = orderService.findById(5L);
        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void shouldThrowWhenOrderByIdNotFound() {
        when(orderRepository.findById(55L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.findById(55L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found");
    }

    // ============ FIND BY USER ============
    @Test
    void shouldFindOrdersByUser() {
        Long userId = 7L;
        Order order = Order.builder().id(1L).build();
        Page<Order> page = new PageImpl<>(List.of(order));
        Pageable pageable = PageRequest.of(0, 1);
        OrderDto.OrderResponse resp = new OrderDto.OrderResponse();
        resp.setId(1L);

        when(orderRepository.findByUserId(userId, pageable)).thenReturn(page);
        when(orderMapper.toOrderResponse(order)).thenReturn(resp);

        Page<OrderDto.OrderResponse> result = orderService.findByUser(userId, pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

    // ============ FIND ALL ============
    @Test
    void shouldFindAllOrders() {
        Order order = Order.builder().id(2L).build();
        Page<Order> page = new PageImpl<>(List.of(order));
        Pageable pageable = PageRequest.of(0, 1);
        OrderDto.OrderResponse resp = new OrderDto.OrderResponse();
        resp.setId(2L);

        when(orderRepository.findAll(pageable)).thenReturn(page);
        when(orderMapper.toOrderResponse(order)).thenReturn(resp);

        Page<OrderDto.OrderResponse> result = orderService.findAll(pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(2L);
    }

    // ============ FIND BY STATUS ============
    @Test
    void shouldFindByStatus() {
        Order order = Order.builder().id(10L).status(Order.Status.PAID).build();
        Page<Order> page = new PageImpl<>(List.of(order));
        Pageable pageable = PageRequest.of(0, 1);
        OrderDto.OrderResponse resp = new OrderDto.OrderResponse();
        resp.setId(10L);

        when(orderRepository.findByStatus(Order.Status.PAID, pageable)).thenReturn(page);
        when(orderMapper.toOrderResponse(order)).thenReturn(resp);

        Page<OrderDto.OrderResponse> result = orderService.findByStatus("PAID", pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(10L);
    }

    // ============ FIND BY USER AND STATUS ============
    @Test
    void shouldFindByUserAndStatus() {
        Long userId = 6L;
        Order order = Order.builder().id(8L).status(Order.Status.PENDING).build();
        Page<Order> page = new PageImpl<>(List.of(order));
        Pageable pageable = PageRequest.of(0, 1);
        OrderDto.OrderResponse resp = new OrderDto.OrderResponse();
        resp.setId(8L);

        when(orderRepository.findByUserIdAndStatus(userId, Order.Status.PENDING, pageable)).thenReturn(page);
        when(orderMapper.toOrderResponse(order)).thenReturn(resp);

        Page<OrderDto.OrderResponse> result = orderService.findByUserAndStatus(userId, "PENDING", pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(8L);
    }

    // ============ UPDATE STATUS ============
    @Test
    void shouldUpdateOrderStatus() {
        Order order = Order.builder().id(77L).status(Order.Status.PENDING).build();
        when(orderRepository.findById(77L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.updateStatus(77L, "SHIPPED");
        assertThat(order.getStatus()).isEqualTo(Order.Status.SHIPPED);
        verify(orderRepository).save(order);
    }

    @Test
    void shouldThrowWhenUpdateStatusOrderNotFound() {
        when(orderRepository.findById(888L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.updateStatus(888L, "SHIPPED"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found");
    }
}
