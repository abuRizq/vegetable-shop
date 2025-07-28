package com.veggieshop.controller;

import com.veggieshop.dto.UserDto;
import com.veggieshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * User Management Controller (Admin and Self).
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    // For registration (user self-signup, not admin create)
    @PostMapping("/register")
    public ResponseEntity<UserDto.UserResponse> register(@RequestBody @Valid UserDto.UserCreateRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    // Get all users (admin only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto.UserResponse>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    // Get user by id (admin or user self)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.user.id")
    public ResponseEntity<UserDto.UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    // Update user (admin or user self)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.user.id")
    public ResponseEntity<UserDto.UserResponse> update(@PathVariable Long id,
                                                       @RequestBody @Valid UserDto.UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    // Delete user (admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
