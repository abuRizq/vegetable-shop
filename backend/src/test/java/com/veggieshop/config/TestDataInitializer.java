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
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds deterministic, repeatable test data for the "test" profile.
 *
 * Best practices:
 * - Scoped to tests only (@Profile("test")) and placed under src/test/java so it never ships to prod.
 * - Idempotent: uses find-or-create and count() guards to avoid duplicates across context reloads.
 * - Provides stable, known credentials for integration tests.
 */
@Configuration
@Profile("test")
@RequiredArgsConstructor
@Slf4j
public class TestDataInitializer {

    public static final String ADMIN_EMAIL = "jane@example.com";   // ADMIN (kept same as dev seeder for test parity)
    public static final String USER_EMAIL  = "john@example.com";   // USER
    public static final String EXTRA_EMAIL = "alice@example.com";  // USER
    public static final String RAW_PASSWORD = "password";

    private final PasswordEncoder passwordEncoder;

    @Bean
    @Transactional
    @SuppressWarnings("unused")
    public CommandLineRunner seedTestData(
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            OfferRepository offerRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository
    ) {
        return args -> {
            log.info("[TestDataInitializer] Seeding test data…");

            // ===== USERS (idempotent) =====
            User user1 = userRepository.findByEmail(USER_EMAIL).orElseGet(() ->
                    userRepository.save(User.builder()
                            .name("John Doe")
                            .email(USER_EMAIL)
                            .password(passwordEncoder.encode(RAW_PASSWORD))
                            .role(User.Role.USER)
                            .enabled(true)
                            .createdAt(Instant.now())
                            .build())
            );

            User admin = userRepository.findByEmail(ADMIN_EMAIL).orElseGet(() ->
                    userRepository.save(User.builder()
                            .name("Jane Smith")
                            .email(ADMIN_EMAIL)
                            .password(passwordEncoder.encode(RAW_PASSWORD))
                            .role(User.Role.ADMIN)
                            .enabled(true)
                            .createdAt(Instant.now())
                            .build())
            );

            User user3 = userRepository.findByEmail(EXTRA_EMAIL).orElseGet(() ->
                    userRepository.save(User.builder()
                            .name("Alice Brown")
                            .email(EXTRA_EMAIL)
                            .password(passwordEncoder.encode(RAW_PASSWORD))
                            .role(User.Role.USER)
                            .enabled(true)
                            .createdAt(Instant.now())
                            .build())
            );

            // ===== CATEGORIES (idempotent) =====
            Category vegetables = categoryRepository.findByName("Vegetables").orElseGet(() ->
                    categoryRepository.save(Category.builder()
                            .name("Vegetables")
                            .description("Fresh vegetables")
                            .build())
            );

            Category fruits = categoryRepository.findByName("Fruits").orElseGet(() ->
                    categoryRepository.save(Category.builder()
                            .name("Fruits")
                            .description("Seasonal fruits")
                            .build())
            );

            Category herbs = categoryRepository.findByName("Herbs").orElseGet(() ->
                    categoryRepository.save(Category.builder()
                            .name("Herbs")
                            .description("Aromatic herbs")
                            .build())
            );

            // ===== PRODUCTS (idempotent) =====
            Product tomato = productRepository.findByName("Tomato").orElseGet(() ->
                    productRepository.save(Product.builder()
                            .name("Tomato")
                            .description("Red juicy tomatoes")
                            .price(new BigDecimal("1.25"))
                            .discount(BigDecimal.ZERO)
                            .featured(true)
                            .soldCount(20L)
                            .imageUrl("https://img.com/tomato.jpg")
                            .active(true)
                            .category(vegetables)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build())
            );

            Product apple = productRepository.findByName("Apple").orElseGet(() ->
                    productRepository.save(Product.builder()
                            .name("Apple")
                            .description("Sweet red apples")
                            .price(new BigDecimal("2.30"))
                            .discount(new BigDecimal("0.20"))
                            .featured(false)
                            .soldCount(10L)
                            .imageUrl("https://img.com/apple.jpg")
                            .active(true)
                            .category(fruits)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build())
            );

            Product basil = productRepository.findByName("Basil").orElseGet(() ->
                    productRepository.save(Product.builder()
                            .name("Basil")
                            .description("Fresh green basil")
                            .price(new BigDecimal("0.99"))
                            .discount(BigDecimal.ZERO)
                            .featured(false)
                            .soldCount(5L)
                            .imageUrl("https://img.com/basil.jpg")
                            .active(true)
                            .category(herbs)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build())
            );

            // ===== OFFERS (guarded) =====
            if (offerRepository.count() == 0) {
                offerRepository.saveAll(List.of(
                        Offer.builder()
                                .product(tomato)
                                .discount(new BigDecimal("0.20"))
                                .startDate(LocalDate.now().minusDays(2))
                                .endDate(LocalDate.now().plusDays(3))
                                .build(),
                        Offer.builder()
                                .product(apple)
                                .discount(new BigDecimal("0.40"))
                                .startDate(LocalDate.now())
                                .endDate(LocalDate.now().plusDays(5))
                                .build(),
                        Offer.builder()
                                .product(basil)
                                .discount(new BigDecimal("0.10"))
                                .startDate(LocalDate.now().minusDays(1))
                                .endDate(LocalDate.now().plusDays(7))
                                .build()
                ));
            }

            // ===== ORDERS + ITEMS (guarded) =====
            if (orderRepository.count() == 0) {
                Order o1 = orderRepository.save(Order.builder()
                        .user(user1)
                        .totalPrice(new BigDecimal("5.15"))
                        .status(Order.Status.PAID)
                        .createdAt(LocalDateTime.now().minusDays(1))
                        .build());

                Order o2 = orderRepository.save(Order.builder()
                        .user(admin)
                        .totalPrice(new BigDecimal("3.20"))
                        .status(Order.Status.SHIPPED)
                        .createdAt(LocalDateTime.now().minusDays(2))
                        .build());

                Order o3 = orderRepository.save(Order.builder()
                        .user(user3)
                        .totalPrice(new BigDecimal("7.50"))
                        .status(Order.Status.PENDING)
                        .createdAt(LocalDateTime.now())
                        .build());

                orderItemRepository.saveAll(List.of(
                        OrderItem.builder().order(o1).product(tomato).quantity(2).price(tomato.getPrice()).build(),
                        OrderItem.builder().order(o1).product(apple).quantity(1).price(apple.getPrice()).build(),
                        OrderItem.builder().order(o2).product(apple).quantity(2).price(apple.getPrice()).build(),
                        OrderItem.builder().order(o3).product(basil).quantity(5).price(basil.getPrice()).build()
                ));
            }

            log.info("[TestDataInitializer] Seeding completed. users={}, categories={}, products={}, offers={}, orders={}",
                    userRepository.count(), categoryRepository.count(), productRepository.count(),
                    offerRepository.count(), orderRepository.count());
        };
    }
}
