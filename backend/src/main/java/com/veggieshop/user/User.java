package com.veggieshop.user;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(columnList = "email", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    // Optional: Enable/disable user (soft delete, suspension)
    @Column(nullable = false)
    private boolean enabled = true;

    // Optional: Track when user registered
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // Optional: For audit/history
    private Instant updatedAt;

    // ========== ROLE ENUM ==========
    public enum Role {
        USER,
        ADMIN
        // Add more roles if needed (e.g., MODERATOR, MANAGER)
    }

    // ========== Audit Hooks (Optional, for auto-update) ==========
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
