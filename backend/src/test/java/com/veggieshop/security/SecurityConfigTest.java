package com.veggieshop.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.*;

class SecurityConfigTest {

    private SecurityConfig securityConfig;
    private JwtAuthFilter jwtAuthFilter;
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setup() {
        jwtAuthFilter = Mockito.mock(JwtAuthFilter.class);
        userDetailsService = Mockito.mock(UserDetailsServiceImpl.class);
        securityConfig = new SecurityConfig(jwtAuthFilter, userDetailsService);
    }

    @Test
    void passwordEncoderBean_shouldReturnBCryptPasswordEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertThat(encoder).isInstanceOf(org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.class);
    }

    @Test
    void authenticationManagerBean_shouldReturnAuthenticationManager() throws Exception {
        AuthenticationConfiguration config = Mockito.mock(AuthenticationConfiguration.class);
        AuthenticationManager expectedManager = Mockito.mock(AuthenticationManager.class);
        Mockito.when(config.getAuthenticationManager()).thenReturn(expectedManager);

        AuthenticationManager manager = securityConfig.authenticationManager(config);
        assertThat(manager).isSameAs(expectedManager);
    }

    @Test
    void securityFilterChainBean_shouldBeCreated() throws Exception {
        // Using an AnnotationConfigApplicationContext for integration of filter bean
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean(JwtAuthFilter.class, () -> jwtAuthFilter);
            ctx.registerBean(UserDetailsServiceImpl.class, () -> userDetailsService);
            ctx.register(SecurityConfig.class);
            ctx.refresh();

            SecurityFilterChain chain = ctx.getBean(SecurityFilterChain.class);
            assertThat(chain).isNotNull();
        }
    }
}
