package com.veggieshop.config;

import com.veggieshop.entity.*;
import com.veggieshop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
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
            // لا تضف البيانات إلا إذا كانت الجداول فارغة
            if (userRepository.count() > 0
                    || categoryRepository.count() > 0
                    || productRepository.count() > 0
                    || offerRepository.count() > 0
                    || orderRepository.count() > 0
                    || orderItemRepository.count() > 0) {
                return;
            }

            // 🟢 Users
            User admin = User.builder()
                    .name("Admin User")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(User.Role.ADMIN)
                    .build();
            User user1 = User.builder()
                    .name("John Doe")
                    .email("john@example.com")
                    .password(passwordEncoder.encode("john123"))
                    .role(User.Role.USER)
                    .build();
            User user2 = User.builder()
                    .name("Jane Smith")
                    .email("jane@example.com")
                    .password(passwordEncoder.encode("jane123"))
                    .role(User.Role.USER)
                    .build();

            userRepository.saveAll(List.of(admin, user1, user2));

            // 🟢 Categories
            Category vegetables = Category.builder()
                    .name("Vegetables")
                    .description("Fresh and organic vegetables.")
                    .build();
            Category fruits = Category.builder()
                    .name("Fruits")
                    .description("Seasonal and imported fruits.")
                    .build();
            Category herbs = Category.builder()
                    .name("Herbs")
                    .description("Aromatic and culinary herbs.")
                    .build();

            categoryRepository.saveAll(List.of(vegetables, fruits, herbs));

            // 🟢 Products
            Product tomato = Product.builder()
                    .name("Tomato")
                    .description("Fresh red tomatoes")
                    .price(BigDecimal.valueOf(2.5))
                    .discount(BigDecimal.ZERO)
                    .featured(true)
                    .soldCount(150L)
                    .imageUrl("https://example.com/tomato.jpg")
                    .category(vegetables)
                    .build();

            Product apple = Product.builder()
                    .name("Apple")
                    .description("Crispy green apples")
                    .price(BigDecimal.valueOf(3))
                    .discount(BigDecimal.valueOf(0.5))
                    .featured(false)
                    .soldCount(90L)
                    .imageUrl("https://example.com/apple.jpg")
                    .category(fruits)
                    .build();

            Product basil = Product.builder()
                    .name("Basil")
                    .description("Organic basil leaves")
                    .price(BigDecimal.valueOf(1.5))
                    .discount(BigDecimal.ZERO)
                    .featured(true)
                    .soldCount(40L)
                    .imageUrl("https://example.com/basil.jpg")
                    .category(herbs)
                    .build();

            productRepository.saveAll(List.of(tomato, apple, basil));

            // 🟢 Offers
            Offer offer1 = Offer.builder()
                    .product(apple)
                    .discount(BigDecimal.valueOf(0.5))
                    .startDate(LocalDate.now().minusDays(3))
                    .endDate(LocalDate.now().plusDays(7))
                    .build();

            Offer offer2 = Offer.builder()
                    .product(tomato)
                    .discount(BigDecimal.valueOf(0.2))
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(10))
                    .build();

            offerRepository.saveAll(List.of(offer1, offer2));

            // 🟢 Orders
            Order order1 = Order.builder()
                    .user(user1)
                    .totalPrice(BigDecimal.valueOf(10))
                    .status(Order.Status.PAID)
                    .orderItems(null)
                    .build();

            Order order2 = Order.builder()
                    .user(user2)
                    .totalPrice(BigDecimal.valueOf(4.5))
                    .status(Order.Status.PENDING)
                    .orderItems(null)
                    .build();

            orderRepository.saveAll(List.of(order1, order2));

            // 🟢 OrderItems
            OrderItem item1 = OrderItem.builder()
                    .order(order1)
                    .product(tomato)
                    .quantity(2)
                    .price(tomato.getPrice())
                    .build();

            OrderItem item2 = OrderItem.builder()
                    .order(order1)
                    .product(apple)
                    .quantity(3)
                    .price(apple.getPrice())
                    .build();

            OrderItem item3 = OrderItem.builder()
                    .order(order2)
                    .product(basil)
                    .quantity(1)
                    .price(basil.getPrice())
                    .build();

            orderItemRepository.saveAll(List.of(item1, item2, item3));

            System.out.println("✅ Database initialized with test data!");
        };
    }
}
