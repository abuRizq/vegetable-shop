package com.veggieshop.order;

import com.veggieshop.offer.Offer;
import com.veggieshop.exception.ResourceNotFoundException;
import com.veggieshop.offer.OfferRepository;
import com.veggieshop.product.Product;
import com.veggieshop.product.ProductRepository;
import com.veggieshop.user.User;
import com.veggieshop.user.UserRepository;
import com.veggieshop.util.PriceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OfferRepository offerRepository;
    private final OrderMapper orderMapper;

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

            // NOTE: Pass Pageable.unpaged() to get all offers
            List<Offer> offers = offerRepository.findByProductId(product.getId(), Pageable.unpaged()).getContent();
            BigDecimal finalPrice = PriceCalculator.calculateFinalPrice(product, offers, java.time.LocalDate.now());

            return OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .price(finalPrice)
                    .build();
        }).toList();

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

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto.OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto.OrderResponse> findByUser(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(orderMapper::toOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto.OrderResponse> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(orderMapper::toOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto.OrderResponse> findByStatus(String status, Pageable pageable) {
        Order.Status orderStatus = Order.Status.valueOf(status.toUpperCase());
        return orderRepository.findByStatus(orderStatus, pageable)
                .map(orderMapper::toOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto.OrderResponse> findByUserAndStatus(Long userId, String status, Pageable pageable) {
        Order.Status orderStatus = Order.Status.valueOf(status.toUpperCase());
        return orderRepository.findByUserIdAndStatus(userId, orderStatus, pageable)
                .map(orderMapper::toOrderResponse);
    }

    @Override
    public void updateStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(Order.Status.valueOf(status.toUpperCase()));
        orderRepository.save(order);
    }
}
