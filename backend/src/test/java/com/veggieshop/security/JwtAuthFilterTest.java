package com.veggieshop.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private UserDetailsService userDetailsService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;
    @Mock private UserDetails userDetails;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private static final String AUTH_HEADER = "Bearer valid.jwt.token";
    private static final String JWT_TOKEN = "valid.jwt.token";
    private static final String USERNAME = "user@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should authenticate user when JWT is valid")
    void testValidJwtAuthenticatesUser() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(AUTH_HEADER);
        when(jwtUtil.getUsernameFromToken(JWT_TOKEN)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(jwtUtil.validateToken(JWT_TOKEN, userDetails)).thenReturn(true);
        when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());
        when(userDetails.getUsername()).thenReturn(USERNAME);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        assert authentication.isAuthenticated();
        assert USERNAME.equals(authentication.getName());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should NOT authenticate when Authorization header is missing")
    void testNoAuthHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assert SecurityContextHolder.getContext().getAuthentication() == null;
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userDetailsService);
    }

    @Test
    @DisplayName("Should NOT authenticate when token is invalid")
    void testInvalidToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(AUTH_HEADER);
        when(jwtUtil.getUsernameFromToken(JWT_TOKEN)).thenThrow(new JwtException("Invalid token"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assert SecurityContextHolder.getContext().getAuthentication() == null;
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("Should NOT authenticate when token is expired")
    void testExpiredToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(AUTH_HEADER);
        when(jwtUtil.getUsernameFromToken(JWT_TOKEN)).thenThrow(new ExpiredJwtException(null, null, "Expired"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assert SecurityContextHolder.getContext().getAuthentication() == null;
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("Should NOT authenticate when token validation fails")
    void testTokenValidationFails() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(AUTH_HEADER);
        when(jwtUtil.getUsernameFromToken(JWT_TOKEN)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(jwtUtil.validateToken(JWT_TOKEN, userDetails)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assert SecurityContextHolder.getContext().getAuthentication() == null;
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should skip if already authenticated")
    void testAlreadyAuthenticated() throws ServletException, IOException {
        Authentication existingAuth = mock(Authentication.class);
        when(existingAuth.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn(AUTH_HEADER);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Should not call userDetailsService.loadUserByUsername at all
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }
}
