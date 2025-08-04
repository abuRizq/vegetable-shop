package com.veggieshop.order;

import com.veggieshop.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private OrderMapper orderMapper;

    @BeforeEach
    void setUp() {
        orderMapper = Mappers.getMapper(OrderMapper.class);
    }

    @Test
    void shouldMapOrderToOrderResponse() {
        User user = User.builder().id(4L).name("User4").build();

        OrderItem orderItem = OrderItem.builder()
                .id(2L)
                .product(null)
                .quantity(5)
                .price(BigDecimal.valueOf(10))
                .build();

        Order order = Order.builder()
                .id(1L)
                .user(user)
                .totalPrice(BigDecimal.valueOf(50))
                .status(Order.Status.PAID)
                .createdAt(LocalDateTime.now())
                .orderItems(List.of(orderItem))
                .build();

        OrderDto.OrderResponse response = orderMapper.toOrderResponse(order);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(4L);
        assertThat(response.getUserName()).isEqualTo("User4");
        assertThat(response.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(response.getStatus()).isEqualTo("PAID");
        assertThat(response.getItems()).hasSize(1);
    }

    @Test
    void shouldReturnNull_whenOrderIsNull() {
        OrderDto.OrderResponse response = orderMapper.toOrderResponse(null);
        assertThat(response).isNull();
    }
}
