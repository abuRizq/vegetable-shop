package com.veggieshop.user;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "User Controller", description = "APIs for user management and registration")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Register new user",
            description = "Self-registration for a new user. No authentication required."
    )
    @PostMapping("/register")
    public ResponseEntity<UserDto.UserResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration data",
                    required = true
            )
            @RequestBody @Valid UserDto.UserCreateRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @Operation(
            summary = "Get all users (paginated)",
            description = "Retrieves all users (ADMIN only, paginated and sortable)."
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto.UserResponse>> getAll(
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    @Operation(
            summary = "Get users by role (paginated)",
            description = "Retrieve users by role (ADMIN only, paginated and sortable)."
    )
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto.UserResponse>> getByRole(
            @PathVariable("role") User.Role role,
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return ResponseEntity.ok(userService.findByRole(role, pageable));
    }

    @Operation(
            summary = "Search users by name or email (paginated)",
            description = "Search users by name or email substring (ADMIN only, paginated)."
    )
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto.UserResponse>> search(
            @RequestParam("q") String query,
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return ResponseEntity.ok(userService.search(query, pageable));
    }

    @Operation(
            summary = "Get user by ID",
            description = "Retrieves a user by ID (ADMIN or user self)."
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.user.id")
    public ResponseEntity<UserDto.UserResponse> getById(
            @Parameter(description = "ID of the user to retrieve", required = true, example = "1")
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @Operation(
            summary = "Update user",
            description = "Update user data (ADMIN or user self)."
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.user.id")
    public ResponseEntity<UserDto.UserResponse> update(
            @Parameter(description = "ID of the user to update", required = true, example = "1")
            @PathVariable("id") Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User update data",
                    required = true
            )
            @RequestBody @Valid UserDto.UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @Operation(
            summary = "Delete user",
            description = "Deletes a user by ID (ADMIN only).",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the user to delete", required = true, example = "1")
            @PathVariable("id") Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
