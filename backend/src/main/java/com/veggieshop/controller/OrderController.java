package com.veggieshop.controller;

import com.veggieshop.dto.OrderDto;
import com.veggieshop.security.CustomUserDetails;
import com.veggieshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order for the authenticated user.
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDto.OrderResponse> create(
            @RequestBody @Valid OrderDto.OrderCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        return ResponseEntity.ok(orderService.create(userId, request));
    }

    /**
     * Get all orders (ADMIN only).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDto.OrderResponse>> getAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    /**
     * Get all orders for a specific user.
     * ADMIN can access any user, USER can access only his own orders.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.user.id")
    public ResponseEntity<List<OrderDto.OrderResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.findByUser(userId));
    }

    /**
     * Get a single order by ID.
     * ADMIN can access any order, USER can access only his own orders.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOrderOwner(#id, principal.user.id)")
    public ResponseEntity<OrderDto.OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    /**
     * Update order status (ADMIN only).
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        orderService.updateStatus(id, status);
        return ResponseEntity.noContent().build();
    }
}
