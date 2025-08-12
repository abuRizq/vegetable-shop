package com.veggieshop.security;

import com.veggieshop.user.User;
import com.veggieshop.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailsServiceImplTest {

    private UserRepository userRepository;
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userDetailsService = new UserDetailsServiceImpl(userRepository);
    }

    @Nested
    @DisplayName("loadUserByUsername")
    class LoadUserByUsername {

        @Test
        @DisplayName("should return UserDetails when user exists")
        void shouldReturnUserDetailsWhenUserExists() {
            // Given
            User user = new User();
            user.setId(1L);
            user.setEmail("test@example.com");
            user.setPassword("hashedPassword");
            user.setRole(User.Role.ADMIN); // adjust if enum or string

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

            // Then
            assertThat(userDetails).isNotNull();
            assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
            assertThat(userDetails.getPassword()).isEqualTo("hashedPassword");
            assertThat(userDetails.getAuthorities()).hasSize(1);
            assertThat(userDetails.isAccountNonExpired()).isTrue();
            assertThat(userDetails.isAccountNonLocked()).isTrue();
            assertThat(userDetails.isCredentialsNonExpired()).isTrue();
            assertThat(userDetails.isEnabled()).isTrue();

            verify(userRepository).findByEmail("test@example.com");
        }

        @Test
        @DisplayName("should throw UsernameNotFoundException when user does not exist")
        void shouldThrowWhenUserDoesNotExist() {
            // Given
            when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

            // Then
            assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@example.com"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("missing@example.com");

            verify(userRepository).findByEmail("missing@example.com");
        }
    }

    @Test
    @DisplayName("should wrap User in CustomUserDetails")
    void shouldWrapUserInCustomUserDetails() {
        // Given
        User user = new User();
        user.setId(42L);
        user.setEmail("wrap@example.com");
        user.setPassword("secret");
        user.setRole(User.Role.USER);

        when(userRepository.findByEmail("wrap@example.com")).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("wrap@example.com");

        // Then
        assertThat(userDetails).isInstanceOf(CustomUserDetails.class);
        CustomUserDetails custom = (CustomUserDetails) userDetails;
        assertThat(custom.getUser()).isEqualTo(user);
    }
}
