package com.veggieshop.user;

import com.veggieshop.common.ApiResponse;
import com.veggieshop.common.ApiError;
import com.veggieshop.common.ApiResponseUtil;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "User Controller", description = "APIs for user management and registration")
public class UserController {

    private final UserService userService;

    // ================== REGISTER NEW USER ==================
    @Operation(
            summary = "Register new user",
            description = "Self-registration for a new user. No authentication required."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate user",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto.UserResponse>> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserDto.UserCreateRequest.class))
            )
            @RequestBody @Valid UserDto.UserCreateRequest request
    ) {
        UserDto.UserResponse registered = userService.register(request);
        return ApiResponseUtil.created(registered);
    }

    // ================== GET ALL USERS (PAGINATED, ADMIN ONLY) ==================
    @Operation(
            summary = "Get all users (paginated)",
            description = "Retrieves all users (ADMIN only, paginated and sortable)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged list of users",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDto.UserResponse>>> getAll(
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        Page<UserDto.UserResponse> page = userService.findAll(pageable);
        return ApiResponseUtil.ok(page);
    }

    // ================== GET USERS BY ROLE (PAGINATED, ADMIN ONLY) ==================
    @Operation(
            summary = "Get users by role (paginated)",
            description = "Retrieve users by role (ADMIN only, paginated and sortable)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged list of users by role",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDto.UserResponse>>> getByRole(
            @PathVariable("role") User.Role role,
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        Page<UserDto.UserResponse> page = userService.findByRole(role, pageable);
        return ApiResponseUtil.ok(page);
    }

    // ================== SEARCH USERS BY NAME OR EMAIL (ADMIN ONLY) ==================
    @Operation(
            summary = "Search users by name or email (paginated)",
            description = "Search users by name or email substring (ADMIN only, paginated)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged search users",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDto.UserResponse>>> search(
            @RequestParam("q") String query,
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        Page<UserDto.UserResponse> page = userService.search(query, pageable);
        return ApiResponseUtil.ok(page);
    }

    // ================== GET USER BY ID (ADMIN OR SELF) ==================
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves a user by ID (ADMIN or user self)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.user.id")
    public ResponseEntity<ApiResponse<UserDto.UserResponse>> getById(
            @Parameter(description = "ID of the user to retrieve", required = true, example = "1")
            @PathVariable("id") @NotNull Long id
    ) {
        return ApiResponseUtil.ok(userService.findById(id));
    }

    // ================== UPDATE USER (ADMIN OR SELF) ==================
    @Operation(
            summary = "Update user",
            description = "Update user data (ADMIN or user self)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.user.id")
    public ResponseEntity<ApiResponse<UserDto.UserResponse>> update(
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
            summary = "Delete user",
            description = "Deletes a user by ID (ADMIN only)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
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
}
