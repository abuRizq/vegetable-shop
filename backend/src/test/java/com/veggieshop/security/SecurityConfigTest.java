package com.veggieshop.security;

import com.veggieshop.security.forDelete.JwtAuthFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecurityConfigTest {

    @MockBean
    JwtAuthFilter jwtAuthFilter;

    @Autowired
    SecurityFilterChain securityFilterChain;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Test
    @DisplayName("SecurityFilterChain bean should exist")
    void testSecurityFilterChainBeanExists() {
        assertThat(securityFilterChain).isNotNull();
    }

    @Test
    @DisplayName("PasswordEncoder bean should exist")
    void testPasswordEncoderBeanExists() {
        assertThat(passwordEncoder).isNotNull();
    }

    @Test
    @DisplayName("AuthenticationManager bean should exist")
    void testAuthenticationManagerBeanExists() {
        assertThat(authenticationManager).isNotNull();
    }

    @Test
    @DisplayName("PasswordEncoder should encode and match password")
    void testPasswordEncoderWorks() {
        String raw = "test123";
        String encoded = passwordEncoder.encode(raw);
        assertThat(passwordEncoder.matches(raw, encoded)).isTrue();
    }
}
