package com.veggieshop.user;

import com.veggieshop.common.ApiResponse;
import com.veggieshop.common.ApiError;
import com.veggieshop.common.ApiResponseUtil;
import com.veggieshop.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "User Controller", description = "APIs for user management and profile")
public class UserController {

    private final UserService userService;

    // ================== GET CURRENT USER PROFILE ==================
    @Operation(
            summary = "Get current user profile",
            description = "Returns the authenticated user's profile."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile data",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class)))
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDto.UserResponse>> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserDto.UserResponse user = userService.findById(userDetails.getId());
        return ApiResponseUtil.ok(user);
    }

    // ================== UPDATE CURRENT USER PROFILE ==================
    @Operation(
            summary = "Update current user profile",
            description = "Update the profile of the authenticated user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDto.UserResponse>> updateMe(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Profile update data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserDto.UserUpdateRequest.class))
            )
            @RequestBody @Valid UserDto.UserUpdateRequest request
    ) {
        UserDto.UserResponse updated = userService.update(userDetails.getId(), request);
        return ApiResponseUtil.ok(updated);
    }

    // ================== CHANGE CURRENT USER PASSWORD ==================
    @Operation(
            summary = "Change current user password",
            description = "Change the password for the authenticated user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid old password",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Password change data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserDto.PasswordChangeRequest.class))
            )
            @RequestBody @Valid UserDto.PasswordChangeRequest request
    ) {
        userService.changePassword(userDetails.getId(), request);
        return ApiResponseUtil.noContent();
    }

    // ================== GET USER BY ID (ADMIN ONLY) ==================
    @Operation(
            summary = "Get user by ID (admin)",
            description = "ADMIN: Retrieve a user by ID."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto.UserResponse>> getById(
            @Parameter(description = "ID of the user to retrieve", required = true, example = "1")
            @PathVariable("id") @NotNull Long id
    ) {
        return ApiResponseUtil.ok(userService.findById(id));
    }

    // ================== GET ALL USERS (ADMIN ONLY) ==================
    @Operation(
            summary = "Get users (paginated, admin only)",
            description = "ADMIN: Get list of users with pagination, search, and sorting."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of users",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDto.UserResponse>>> getAll(
            @RequestParam(value = "q", required = false) String query,
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        Page<UserDto.UserResponse> page;
        if (query != null && !query.isEmpty()) {
            page = userService.search(query, pageable);
        } else {
            page = userService.findAll(pageable);
        }
        return ApiResponseUtil.ok(page);
    }

    // ================== ADD USER (ADMIN ONLY) ==================
    @Operation(
            summary = "Add new user (admin only)",
            description = "ADMIN: Create new user (useful for creating admins)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate email",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto.UserResponse>> addUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User creation data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserDto.UserCreateRequest.class))
            )
            @RequestBody @Valid UserDto.UserCreateRequest request
    ) {
        UserDto.UserResponse created = userService.register(request);
        return ApiResponseUtil.created(created);
    }

    // ================== UPDATE USER (ADMIN ONLY) ==================
    @Operation(
            summary = "Update user by ID (admin only)",
            description = "ADMIN: Update another user's data."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto.UserResponse>> updateUser(
            @Parameter(description = "ID of the user to update", required = true, example = "1")
            @PathVariable("id") @NotNull Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User update data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserDto.UserUpdateRequest.class))
            )
            @RequestBody @Valid UserDto.UserUpdateRequest request
    ) {
        UserDto.UserResponse updated = userService.update(id, request);
        return ApiResponseUtil.ok(updated);
    }

    // ================== DELETE USER (ADMIN ONLY) ==================
    @Operation(
            summary = "Delete user (admin only)",
            description = "ADMIN: Delete user by ID."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "User deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID of the user to delete", required = true, example = "1")
            @PathVariable("id") @NotNull Long id
    ) {
        userService.delete(id);
        return ApiResponseUtil.noContent();
    }

    // ================== CHANGE USER ROLE (ADMIN ONLY) ==================
    @Operation(
            summary = "Change user role (admin only)",
            description = "ADMIN: Change the role of a user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role changed",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto.UserResponse>> changeRole(
            @Parameter(description = "ID of the user", required = true, example = "1")
            @PathVariable("id") @NotNull Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Role change request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserDto.RoleChangeRequest.class))
            )
            @RequestBody @Valid UserDto.RoleChangeRequest request
    ) {
        UserDto.UserResponse updated = userService.changeRole(id, request.getRole());
        return ApiResponseUtil.ok(updated);
    }
}
