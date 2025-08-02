package com.veggieshop.auth;

import com.veggieshop.common.ApiResponse;
import com.veggieshop.common.ApiError;
import com.veggieshop.common.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

// === OpenAPI Annotations ===
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Auth Controller", description = "APIs for authentication (login and JWT token generation)")
public class AuthController {

    private final AuthService authService;

    // ================== USER LOGIN ==================
    @Operation(
            summary = "User login",
            description = "Login with email and password. Returns a JWT token in the response."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful, JWT token returned",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or bad credentials",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials (email and password)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AuthDto.AuthRequest.class))
            )
            @RequestBody @Valid AuthDto.AuthRequest request
    ) {
        AuthDto.AuthResponse authResponse = authService.login(request);
        return ApiResponseUtil.ok(authResponse);
    }
}
