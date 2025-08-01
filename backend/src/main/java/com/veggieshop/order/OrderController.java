package com.veggieshop.order;

import com.veggieshop.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

// === OpenAPI Annotations ===
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
@Tag(name = "Order Controller", description = "APIs for managing orders and order status")
public class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Create a new order",
            description = "Create a new order for the authenticated user. Requires USER role."
    )
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDto.OrderResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Order creation data",
                    required = true
            )
            @RequestBody @Valid OrderDto.OrderCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        return ResponseEntity.ok(orderService.create(userId, request));
    }

    @Operation(
            summary = "Get all orders (ADMIN only)",
            description = "Retrieves all orders in the system. Requires ADMIN role."
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDto.OrderResponse>> getAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @Operation(
            summary = "Get all orders for a specific user",
            description = "Retrieves all orders for a specific user. ADMIN can access any user, USER can access only his own orders."
    )
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.user.id")
    public ResponseEntity<List<OrderDto.OrderResponse>> getByUser(
            @Parameter(description = "ID of the user", required = true, example = "1")
            @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(orderService.findByUser(userId));
    }

    @Operation(
            summary = "Get order by ID",
            description = "Retrieves a single order by its ID. ADMIN can access any order, USER can access only his own orders."
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOrderOwner(#id, principal.user.id)")
    public ResponseEntity<OrderDto.OrderResponse> getById(
            @Parameter(description = "ID of the order to retrieve", required = true, example = "1")
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @Operation(
            summary = "Update order status (ADMIN only)",
            description = "Updates the status of an order by its ID. Requires ADMIN role.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Order status updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid status value or order not updatable"),
                    @ApiResponse(responseCode = "404", description = "Order not found")
            }
    )
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStatus(
            @Parameter(description = "ID of the order to update", required = true, example = "1")
            @PathVariable("id") Long id,
            @Parameter(description = "New status for the order", required = true, example = "SHIPPED")
            @RequestParam("status") String status) {
        orderService.updateStatus(id, status);
        return ResponseEntity.noContent().build();
    }
}
