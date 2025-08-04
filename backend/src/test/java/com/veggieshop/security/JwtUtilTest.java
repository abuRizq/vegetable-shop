package com.veggieshop.security;

import io.jsonwebtoken.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    // You may use a hard-coded secret for testing
    private static final String SECRET = "test-jwt-secret-key-for-unittest-1234567890";
    private static final long EXPIRATION_MS = 1000 * 60 * 60; // 1 hour

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();

        // Set private fields via reflection
        Field secretField = JwtUtil.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(jwtUtil, SECRET);

        Field expField = JwtUtil.class.getDeclaredField("jwtExpirationMs");
        expField.setAccessible(true);
        expField.set(jwtUtil, EXPIRATION_MS);
    }

    @Test
    void generateAccessToken_and_parse_username_success() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user@mail.com");

        String token = jwtUtil.generateAccessToken(userDetails);

        assertThat(token).isNotBlank();

        String username = jwtUtil.getUsernameFromToken(token);
        assertThat(username).isEqualTo("user@mail.com");
    }

    @Test
    void validateToken_shouldReturnTrue_forValidToken() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user@mail.com");

        String token = jwtUtil.generateAccessToken(userDetails);

        boolean valid = jwtUtil.validateToken(token, userDetails);
        assertThat(valid).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalse_forExpiredToken() throws Exception {
        // Create an expired token manually
        String expiredToken = Jwts.builder()
                .setSubject("expired@mail.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 7200000))
                .setExpiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("expired@mail.com");

        assertThatThrownBy(() -> jwtUtil.validateToken(expiredToken, userDetails))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token expired");
    }

    @Test
    void isTokenExpired_shouldReturnFalse_forFreshToken() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@mail.com");
        String token = jwtUtil.generateAccessToken(userDetails);

        boolean expired = jwtUtil.isTokenExpired(token);
        assertThat(expired).isFalse();
    }

    @Test
    void isTokenExpired_shouldReturnTrue_forExpiredToken() {
        String expiredToken = Jwts.builder()
                .setSubject("any")
                .setIssuedAt(new Date(System.currentTimeMillis() - 100000))
                .setExpiration(new Date(System.currentTimeMillis() - 100))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();

        assertThatThrownBy(() -> jwtUtil.isTokenExpired(expiredToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token expired");
    }

    @Test
    void getClaimFromToken_shouldExtractClaim() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@mail.com");
        String token = jwtUtil.generateAccessToken(userDetails);

        String subject = jwtUtil.getClaimFromToken(token, Claims::getSubject);
        assertThat(subject).isEqualTo("test@mail.com");
    }

    @Test
    void parseToken_shouldThrowIllegalArgumentException_forMalformedToken() {
        String invalidToken = "invalid.token.value";

        assertThatThrownBy(() -> jwtUtil.getUsernameFromToken(invalidToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid JWT token");
    }
}
