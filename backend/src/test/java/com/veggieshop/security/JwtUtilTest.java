package com.veggieshop.security;

import com.veggieshop.security.forDelete.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private static final String SECRET = Base64.getEncoder().encodeToString(SECRET_KEY.getEncoded());
    private static final long EXPIRATION_MS = 1000 * 60 * 5; // 5 minutes

    private final String username = "testuser@example.com";

    private final UserDetails userDetails = org.mockito.Mockito.mock(UserDetails.class);

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        // Inject secret and expiration via reflection
        Field secretField = JwtUtil.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(jwtUtil, SECRET);

        Field expField = JwtUtil.class.getDeclaredField("jwtExpirationMs");
        expField.setAccessible(true);
        expField.set(jwtUtil, EXPIRATION_MS);

        org.mockito.Mockito.when(userDetails.getUsername()).thenReturn(username);
    }

    @Test
    @DisplayName("Should generate token with correct username as subject")
    void testGenerateTokenAndParseUsername() {
        String token = jwtUtil.generateAccessToken(userDetails);
        String parsedUsername = jwtUtil.getUsernameFromToken(token);

        assertThat(parsedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("Should validate token for correct user")
    void testValidateTokenSuccess() {
        String token = jwtUtil.generateAccessToken(userDetails);

        boolean isValid = jwtUtil.validateToken(token, userDetails);
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should fail validation if username does not match")
    void testValidateTokenWrongUsername() {
        String token = jwtUtil.generateAccessToken(userDetails);

        UserDetails otherUser = org.mockito.Mockito.mock(UserDetails.class);
        org.mockito.Mockito.when(otherUser.getUsername()).thenReturn("someoneelse@example.com");

        boolean isValid = jwtUtil.validateToken(token, otherUser);
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should fail validation for expired token")
    void testValidateTokenExpired() throws Exception {
        // Use reflection to set a very short expiration for this test
        Field expField = JwtUtil.class.getDeclaredField("jwtExpirationMs");
        expField.setAccessible(true);
        expField.set(jwtUtil, 1L); // 1 ms

        String token = jwtUtil.generateAccessToken(userDetails);
        // Sleep to ensure token is expired
        Thread.sleep(10);

        boolean isValid = jwtUtil.validateToken(token, userDetails);
        assertThat(isValid).isFalse();
        // isTokenExpired should return true
        assertThat(jwtUtil.isTokenExpired(token)).isTrue();
    }

    @Test
    @DisplayName("Should throw JwtException for malformed token")
    void testMalformedTokenThrows() {
        String invalidToken = "not_a_real_jwt_token";
        assertThatThrownBy(() -> jwtUtil.getUsernameFromToken(invalidToken))
                .isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> jwtUtil.isTokenExpired(invalidToken))
                .isInstanceOf(JwtException.class);
        assertThat(jwtUtil.validateToken(invalidToken, userDetails)).isFalse();
    }

    @Test
    @DisplayName("Should throw ExpiredJwtException for expired token in getClaimFromToken")
    void testGetClaimFromExpiredTokenThrows() throws Exception {
        // Use reflection to set expiration to 1 ms
        Field expField = JwtUtil.class.getDeclaredField("jwtExpirationMs");
        expField.setAccessible(true);
        expField.set(jwtUtil, 1L);

        String token = jwtUtil.generateAccessToken(userDetails);
        Thread.sleep(10);

        assertThatThrownBy(() -> jwtUtil.getUsernameFromToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
