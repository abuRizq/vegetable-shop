package com.veggieshop.controller;

import com.veggieshop.dto.AuthDto;
import com.veggieshop.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

// === OpenAPI Annotations ===
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Auth Controller", description = "APIs for authentication (login and JWT token generation)")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "User login",
            description = "Login with email and password. Returns a JWT token in the response."
    )
    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials (email and password)", required = true
            )
            @RequestBody @Valid AuthDto.AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
