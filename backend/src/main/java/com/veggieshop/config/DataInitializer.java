package com.veggieshop.config;

import com.veggieshop.category.Category;
import com.veggieshop.category.CategoryRepository;
import com.veggieshop.offer.Offer;
import com.veggieshop.offer.OfferRepository;
import com.veggieshop.order.Order;
import com.veggieshop.order.OrderItem;
import com.veggieshop.order.OrderItemRepository;
import com.veggieshop.order.OrderRepository;
import com.veggieshop.product.Product;
import com.veggieshop.product.ProductRepository;
import com.veggieshop.user.User;
import com.veggieshop.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            OfferRepository offerRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository
    ) {
        return args -> {
            if (userRepository.count() > 0
                    || categoryRepository.count() > 0
                    || productRepository.count() > 0
                    || offerRepository.count() > 0
                    || orderRepository.count() > 0
                    || orderItemRepository.count() > 0) {
                return;
            }

            // === USERS ===
            User user1 = userRepository.save(User.builder()
                    .name("John Doe")
                    .email("john@example.com")
                    .password(passwordEncoder.encode("password"))
                    .role(User.Role.USER)
                    .enabled(true)
                    .createdAt(Instant.now())
                    .build());

            User user2 = userRepository.save(User.builder()
                    .name("Jane Smith")
                    .email("jane@example.com")
                    .password(passwordEncoder.encode("password"))
                    .role(User.Role.ADMIN)
                    .enabled(true)
                    .createdAt(Instant.now())
                    .build());

            User user3 = userRepository.save(User.builder()
                    .name("Alice Brown")
                    .email("alice@example.com")
                    .password(passwordEncoder.encode("password"))
                    .role(User.Role.USER)
                    .enabled(true)
                    .createdAt(Instant.now())
                    .build());

            // === CATEGORIES ===
            Category cat1 = categoryRepository.save(Category.builder().name("Vegetables").description("Fresh vegetables").build());
            Category cat2 = categoryRepository.save(Category.builder().name("Fruits").description("Seasonal fruits").build());
            Category cat3 = categoryRepository.save(Category.builder().name("Herbs").description("Aromatic herbs").build());

            // === PRODUCTS ===
            Product prod1 = productRepository.save(Product.builder()
                    .name("Tomato")
                    .description("Red juicy tomatoes")
                    .price(new BigDecimal("1.25"))
                    .discount(BigDecimal.ZERO)
                    .featured(true)
                    .soldCount(20L)
                    .imageUrl("https://img.com/tomato.jpg")
                    .active(true)
                    .category(cat1)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            Product prod2 = productRepository.save(Product.builder()
                    .name("Apple")
                    .description("Sweet red apples")
                    .price(new BigDecimal("2.30"))
                    .discount(new BigDecimal("0.20"))
                    .featured(false)
                    .soldCount(10L)
                    .imageUrl("https://img.com/apple.jpg")
                    .active(true)
                    .category(cat2)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            Product prod3 = productRepository.save(Product.builder()
                    .name("Basil")
                    .description("Fresh green basil")
                    .price(new BigDecimal("0.99"))
                    .discount(BigDecimal.ZERO)
                    .featured(false)
                    .soldCount(5L)
                    .imageUrl("https://img.com/basil.jpg")
                    .active(true)
                    .category(cat3)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            // === OFFERS ===
            Offer offer1 = offerRepository.save(Offer.builder()
                    .product(prod1)
                    .discount(new BigDecimal("0.20"))
                    .startDate(LocalDate.now().minusDays(2))
                    .endDate(LocalDate.now().plusDays(3))
                    .build());

            Offer offer2 = offerRepository.save(Offer.builder()
                    .product(prod2)
                    .discount(new BigDecimal("0.40"))
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(5))
                    .build());

            Offer offer3 = offerRepository.save(Offer.builder()
                    .product(prod3)
                    .discount(new BigDecimal("0.10"))
                    .startDate(LocalDate.now().minusDays(1))
                    .endDate(LocalDate.now().plusDays(7))
                    .build());

            // === ORDERS + ORDER ITEMS ===
            Order order1 = Order.builder()
                    .user(user1)
                    .totalPrice(new BigDecimal("5.15"))
                    .status(Order.Status.PAID)
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build();
            order1 = orderRepository.save(order1);

            Order order2 = Order.builder()
                    .user(user2)
                    .totalPrice(new BigDecimal("3.20"))
                    .status(Order.Status.SHIPPED)
                    .createdAt(LocalDateTime.now().minusDays(2))
                    .build();
            order2 = orderRepository.save(order2);

            Order order3 = Order.builder()
                    .user(user3)
                    .totalPrice(new BigDecimal("7.50"))
                    .status(Order.Status.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
            order3 = orderRepository.save(order3);

            orderItemRepository.saveAll(List.of(
                    OrderItem.builder().order(order1).product(prod1).quantity(2).price(prod1.getPrice()).build(),
                    OrderItem.builder().order(order1).product(prod2).quantity(1).price(prod2.getPrice()).build(),
                    OrderItem.builder().order(order2).product(prod2).quantity(2).price(prod2.getPrice()).build(),
                    OrderItem.builder().order(order3).product(prod3).quantity(5).price(prod3.getPrice()).build()
            ));
        };
    }
}
