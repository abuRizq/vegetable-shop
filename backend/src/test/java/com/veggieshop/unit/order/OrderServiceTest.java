package com.veggieshop.unit.order;

import com.veggieshop.exception.ResourceNotFoundException;
import com.veggieshop.offer.OfferRepository;
import com.veggieshop.order.*;
import com.veggieshop.order.Order;
import com.veggieshop.product.Product;
import com.veggieshop.product.ProductRepository;
import com.veggieshop.user.User;
import com.veggieshop.user.repository.UserRepository;
import com.veggieshop.util.PriceCalculator;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OfferRepository offerRepository;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private AutoCloseable closeable;

    private final User user = User.builder().id(2L).name("Regular User").build();
    private final Product tomato = Product.builder().id(1L).name("Fresh Tomato").soldCount(10L).price(BigDecimal.valueOf(2.00)).build();
    private final Product cucumber = Product.builder().id(3L).name("Cucumber (Large Pack)").soldCount(5L).price(BigDecimal.valueOf(1.99)).build();

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // =========== Create ===========
    @Test
    void create_shouldSucceed_whenValid() {
        OrderItemDto.OrderItemCreateRequest item1 = new OrderItemDto.OrderItemCreateRequest();
        item1.setProductId(1L);
        item1.setQuantity(2);

        OrderItemDto.OrderItemCreateRequest item2 = new OrderItemDto.OrderItemCreateRequest();
        item2.setProductId(3L);
        item2.setQuantity(1);

        OrderDto.OrderCreateRequest req = new OrderDto.OrderCreateRequest();
        req.setItems(List.of(item1, item2));

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(tomato));
        when(productRepository.findById(3L)).thenReturn(Optional.of(cucumber));

        // لا توجد عروض خاصة في هذا السيناريو (dummy)
        when(offerRepository.findByProductId(eq(1L), any(Pageable.class))).thenReturn(Page.empty());
        when(offerRepository.findByProductId(eq(3L), any(Pageable.class))).thenReturn(Page.empty());

        // PriceCalculator يجب أن يكون static utility (mock with real)
        try (MockedStatic<PriceCalculator> priceCalculator = mockStatic(PriceCalculator.class)) {
            priceCalculator.when(() -> PriceCalculator.calculateFinalPrice(any(Product.class), anyList(), any(LocalDate.class)))
                    .thenAnswer(invocation -> {
                        Product p = invocation.getArgument(0);
                        return p.getPrice();
                    });

            // build order + items
            com.veggieshop.order.Order order = com.veggieshop.order.Order.builder()
                    .id(7L)
                    .user(user)
                    .status(com.veggieshop.order.Order.Status.PENDING)
                    .totalPrice(BigDecimal.valueOf(5.99))
                    .createdAt(LocalDateTime.now())
                    .orderItems(new ArrayList<>()) // Will be set in test
                    .build();

            OrderItem oi1 = OrderItem.builder().id(1L).order(order).product(tomato).quantity(2).price(tomato.getPrice()).build();
            OrderItem oi2 = OrderItem.builder().id(2L).order(order).product(cucumber).quantity(1).price(cucumber.getPrice()).build();
            List<OrderItem> items = List.of(oi1, oi2);
            order.setOrderItems(items);

            when(orderRepository.save(any(com.veggieshop.order.Order.class))).thenReturn(order);

            OrderDto.OrderResponse response = new OrderDto.OrderResponse();
            response.setId(7L);
            response.setUserId(2L);
            response.setUserName("Regular User");
            response.setTotalPrice(BigDecimal.valueOf(5.99));
            response.setStatus("PENDING");
            when(orderMapper.toOrderResponse(order)).thenReturn(response);

            OrderDto.OrderResponse result = orderService.create(2L, req);

            assertThat(result.getId()).isEqualTo(7L);
            assertThat(result.getTotalPrice()).isEqualTo(BigDecimal.valueOf(5.99));
            assertThat(result.getStatus()).isEqualTo("PENDING");
            verify(orderRepository).save(any(com.veggieshop.order.Order.class));
            verify(productRepository, atLeastOnce()).save(any(Product.class)); // soldCount update
        }
    }

    @Test
    void create_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        OrderDto.OrderCreateRequest req = new OrderDto.OrderCreateRequest();
        req.setItems(List.of());
        assertThrows(ResourceNotFoundException.class, () -> orderService.create(99L, req));
    }

    @Test
    void create_shouldThrow_whenProductNotFound() {
        OrderItemDto.OrderItemCreateRequest item1 = new OrderItemDto.OrderItemCreateRequest();
        item1.setProductId(99L);
        item1.setQuantity(2);
        OrderDto.OrderCreateRequest req = new OrderDto.OrderCreateRequest();
        req.setItems(List.of(item1));

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.create(2L, req));
    }

    // =========== Find By Id ===========
    @Test
    void findById_shouldReturnOrder_whenExists() {
        com.veggieshop.order.Order order = com.veggieshop.order.Order.builder().id(1L).user(user).status(com.veggieshop.order.Order.Status.PAID).totalPrice(BigDecimal.TEN).createdAt(LocalDateTime.now()).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        OrderDto.OrderResponse resp = new OrderDto.OrderResponse();
        resp.setId(1L);
        resp.setUserId(2L);
        resp.setStatus("PAID");
        resp.setTotalPrice(BigDecimal.TEN);
        when(orderMapper.toOrderResponse(order)).thenReturn(resp);

        OrderDto.OrderResponse result = orderService.findById(1L);

        assertThat(result.getStatus()).isEqualTo("PAID");
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_shouldThrow_whenOrderMissing() {
        when(orderRepository.findById(111L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.findById(111L));
    }

    // =========== findAll ===========
    @Test
    void findAll_shouldReturnPagedOrders() {
        Pageable pageable = PageRequest.of(0, 2);
        com.veggieshop.order.Order o1 = com.veggieshop.order.Order.builder().id(1L).user(user).build();
        com.veggieshop.order.Order o2 = com.veggieshop.order.Order.builder().id(2L).user(user).build();
        Page<com.veggieshop.order.Order> page = new PageImpl<>(List.of(o1, o2), pageable, 2);

        when(orderRepository.findAll(pageable)).thenReturn(page);
        when(orderMapper.toOrderResponse(o1)).thenReturn(new OrderDto.OrderResponse());
        when(orderMapper.toOrderResponse(o2)).thenReturn(new OrderDto.OrderResponse());

        Page<OrderDto.OrderResponse> result = orderService.findAll(pageable);
        assertThat(result.getContent()).hasSize(2);
    }

    // =========== findByUser ===========
    @Test
    void findByUser_shouldReturnUserOrders() {
        Pageable pageable = PageRequest.of(0, 1);
        com.veggieshop.order.Order o1 = com.veggieshop.order.Order.builder().id(1L).user(user).build();
        Page<com.veggieshop.order.Order> page = new PageImpl<>(List.of(o1), pageable, 1);

        when(orderRepository.findByUserId(2L, pageable)).thenReturn(page);
        when(orderMapper.toOrderResponse(o1)).thenReturn(new OrderDto.OrderResponse());

        Page<OrderDto.OrderResponse> result = orderService.findByUser(2L, pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    // =========== findByStatus ===========
    @Test
    void findByStatus_shouldReturnOrdersWithStatus() {
        Pageable pageable = PageRequest.of(0, 2);
        com.veggieshop.order.Order o1 = com.veggieshop.order.Order.builder().id(1L).user(user).status(com.veggieshop.order.Order.Status.PAID).build();
        Page<com.veggieshop.order.Order> page = new PageImpl<>(List.of(o1), pageable, 1);

        when(orderRepository.findByStatus(com.veggieshop.order.Order.Status.PAID, pageable)).thenReturn(page);
        when(orderMapper.toOrderResponse(o1)).thenReturn(new OrderDto.OrderResponse());

        Page<OrderDto.OrderResponse> result = orderService.findByStatus("PAID", pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    // =========== findByUserAndStatus ===========
    @Test
    void findByUserAndStatus_shouldReturnOrdersForUserWithStatus() {
        Pageable pageable = PageRequest.of(0, 2);
        com.veggieshop.order.Order o1 = com.veggieshop.order.Order.builder().id(1L).user(user).status(com.veggieshop.order.Order.Status.SHIPPED).build();
        Page<com.veggieshop.order.Order> page = new PageImpl<>(List.of(o1), pageable, 1);

        when(orderRepository.findByUserIdAndStatus(2L, com.veggieshop.order.Order.Status.SHIPPED, pageable)).thenReturn(page);
        when(orderMapper.toOrderResponse(o1)).thenReturn(new OrderDto.OrderResponse());

        Page<OrderDto.OrderResponse> result = orderService.findByUserAndStatus(2L, "SHIPPED", pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    // =========== updateStatus ===========
    @Test
    void updateStatus_shouldUpdateOrderStatus() {
        com.veggieshop.order.Order order = com.veggieshop.order.Order.builder().id(9L).user(user).status(com.veggieshop.order.Order.Status.PENDING).build();
        when(orderRepository.findById(9L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.updateStatus(9L, "CANCELLED");

        assertThat(order.getStatus()).isEqualTo(Order.Status.CANCELLED);
        verify(orderRepository).save(order);
    }

    @Test
    void updateStatus_shouldThrow_whenOrderMissing() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.updateStatus(404L, "SHIPPED"));
    }
}
