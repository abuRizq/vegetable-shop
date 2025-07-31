package com.veggieshop.service;

import com.veggieshop.dto.OrderDto;
import com.veggieshop.dto.OrderItemDto;
import com.veggieshop.entity.*;
import com.veggieshop.exception.ResourceNotFoundException;
import com.veggieshop.repository.*;
import com.veggieshop.util.PriceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OfferRepository offerRepository;

    @Override
    public OrderDto.OrderResponse create(Long userId, OrderDto.OrderCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = Order.builder()
                .user(user)
                .status(Order.Status.PENDING)
                .build();

        List<OrderItem> items = request.getItems().stream().map(itemReq -> {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            // Get all offers for this product
            List<Offer> offers = offerRepository.findByProductId(product.getId());

            // Calculate the final price with product discount and active offer(s)
            BigDecimal finalPrice = PriceCalculator.calculateFinalPrice(product, offers, java.time.LocalDate.now());

            return OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .price(finalPrice)
                    .build();
        }).collect(Collectors.toList());

        // Calculate total price
        BigDecimal total = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(total);
        order.setOrderItems(items);
        Order savedOrder = orderRepository.save(order);

        // Update sold count for each product
        items.forEach(item -> {
            Product product = item.getProduct();
            product.setSoldCount(product.getSoldCount() + item.getQuantity());
            productRepository.save(product);
        });

        return mapToResponse(savedOrder);
    }


    @Override
    @Transactional(readOnly = true)
    public OrderDto.OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto.OrderResponse> findByUser(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto.OrderResponse> findAll() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void updateStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(Order.Status.valueOf(status.toUpperCase()));
        orderRepository.save(order);
    }

    private OrderDto.OrderResponse mapToResponse(Order order) {
        OrderDto.OrderResponse dto = new OrderDto.OrderResponse();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setUserName(order.getUser().getName());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus().name());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setItems(order.getOrderItems().stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList()));
        return dto;
    }

    private OrderItemDto.OrderItemResponse mapItemToResponse(OrderItem item) {
        OrderItemDto.OrderItemResponse dto = new OrderItemDto.OrderItemResponse();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        return dto;
    }
}
