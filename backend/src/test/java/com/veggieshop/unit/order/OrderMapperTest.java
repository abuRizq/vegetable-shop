package com.veggieshop.unit.order;

import com.veggieshop.order.*;
import com.veggieshop.product.Product;
import com.veggieshop.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private OrderMapper orderMapper;
    private OrderItemMapper orderItemMapper;

    @BeforeEach
    void setUp() throws Exception {
        orderMapper = Mappers.getMapper(OrderMapper.class);
        orderItemMapper = Mappers.getMapper(OrderItemMapper.class);

        // Inject orderItemMapper manually
        Field field = orderMapper.getClass().getDeclaredField("orderItemMapper");
        field.setAccessible(true);
        field.set(orderMapper, orderItemMapper);
    }

    @Test
    void toOrderResponse_shouldMapAllFieldsAndOrderItemsCorrectly() {
        // Arrange
        User user = User.builder()
                .id(2L)
                .name("Regular User")
                .email("user@veggieshop.com")
                .build();

        Product product1 = Product.builder()
                .id(1L)
                .name("Fresh Tomato")
                .build();
        Product product2 = Product.builder()
                .id(3L)
                .name("Cucumber (Large Pack)")
                .build();

        Order order = Order.builder()
                .id(42L)
                .user(user)
                .totalPrice(new BigDecimal("5.99"))
                .status(Order.Status.PAID)
                .createdAt(LocalDateTime.of(2024, 8, 5, 12, 30))
                .orderItems(List.of(
                        OrderItem.builder()
                                .id(101L)
                                .order(null) // Avoid cycles, not needed in mapping
                                .product(product1)
                                .quantity(2)
                                .price(new BigDecimal("2.00"))
                                .build(),
                        OrderItem.builder()
                                .id(102L)
                                .order(null)
                                .product(product2)
                                .quantity(1)
                                .price(new BigDecimal("1.99"))
                                .build()
                ))
                .build();

        // Act
        OrderDto.OrderResponse dto = orderMapper.toOrderResponse(order);

        // Assert: Fields from Order + User
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(order.getId());
        assertThat(dto.getUserId()).isEqualTo(user.getId());
        assertThat(dto.getUserName()).isEqualTo(user.getName());
        assertThat(dto.getTotalPrice()).isEqualTo(order.getTotalPrice());
        assertThat(dto.getStatus()).isEqualTo(order.getStatus().name());
        assertThat(dto.getCreatedAt()).isEqualTo(order.getCreatedAt());

        // Assert: OrderItems mapped properly
        assertThat(dto.getItems()).hasSize(2);

        OrderItemDto.OrderItemResponse item1 = dto.getItems().get(0);
        assertThat(item1.getId()).isEqualTo(101L);
        assertThat(item1.getProductId()).isEqualTo(product1.getId());
        assertThat(item1.getProductName()).isEqualTo(product1.getName());
        assertThat(item1.getQuantity()).isEqualTo(2);
        assertThat(item1.getPrice()).isEqualTo(new BigDecimal("2.00"));

        OrderItemDto.OrderItemResponse item2 = dto.getItems().get(1);
        assertThat(item2.getId()).isEqualTo(102L);
        assertThat(item2.getProductId()).isEqualTo(product2.getId());
        assertThat(item2.getProductName()).isEqualTo(product2.getName());
        assertThat(item2.getQuantity()).isEqualTo(1);
        assertThat(item2.getPrice()).isEqualTo(new BigDecimal("1.99"));
    }

    @Test
    void toOrderResponse_shouldReturnNull_whenOrderIsNull() {
        assertThat(orderMapper.toOrderResponse(null)).isNull();
    }
}
