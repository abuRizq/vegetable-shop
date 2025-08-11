package com.veggieshop.order;

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
import jakarta.validation.constraints.Min;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
@Tag(name = "Order Controller", description = "APIs for managing orders and order status")
public class OrderController {

    private final OrderService orderService;

    // ================== CREATE NEW ORDER (USER ONLY) ==================
    @Operation(
            summary = "Create a new order",
            description = "Create a new order for the authenticated user. Requires USER role."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Order created",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate order",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<OrderDto.OrderResponse>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Order creation data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = OrderDto.OrderCreateRequest.class))
            )
            @RequestBody @Valid OrderDto.OrderCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        OrderDto.OrderResponse created = orderService.create(userId, request);
        return ApiResponseUtil.created(created);
    }

    // ================== GET ALL ORDERS (ADMIN ONLY, PAGED) ==================
    @Operation(
            summary = "Get all orders (paged, ADMIN only)",
            description = "Retrieves all orders in the system with pagination. Requires ADMIN role."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged list of all orders",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderDto.OrderResponse>>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<OrderDto.OrderResponse> page = orderService.findAll(pageable);
        return ApiResponseUtil.ok(page);
    }

    // ================== GET ALL ORDERS FOR A USER (PAGED) ==================
    @Operation(
            summary = "Get all orders for a specific user (paged)",
            description = "Retrieves all orders for a specific user, paged. ADMIN can access any user, USER can access only his own orders."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged list of user orders",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.user.id")
    public ResponseEntity<ApiResponse<List<OrderDto.OrderResponse>>> getByUser(
            @Parameter(description = "ID of the user", required = true, example = "1")
            @PathVariable("userId") @NotNull @Min(1) Long userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<OrderDto.OrderResponse> page = orderService.findByUser(userId, pageable);
        return ApiResponseUtil.ok(page);
    }

    // ================== GET ORDERS BY STATUS (ADMIN ONLY, PAGED) ==================
    @Operation(
            summary = "Get orders by status (paged, ADMIN only)",
            description = "Retrieves orders filtered by status (e.g., PAID, PENDING), paged."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged orders by status",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderDto.OrderResponse>>> getByStatus(
            @Parameter(description = "Status of the orders", required = true, example = "PAID")
            @PathVariable("status") @NotNull String status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        Page<OrderDto.OrderResponse> page = orderService.findByStatus(status, pageable);
        return ApiResponseUtil.ok(page);
    }

    // ================== GET ORDERS BY USER AND STATUS (PAGED) ==================
    @Operation(
            summary = "Get orders by user and status (paged)",
            description = "Retrieves orders for a user filtered by status, paged."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged user orders by status",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @GetMapping("/user/{userId}/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.user.id")
    public ResponseEntity<ApiResponse<List<OrderDto.OrderResponse>>> getByUserAndStatus(
            @PathVariable("userId") @NotNull @Min(1) Long userId,
            @PathVariable("status") @NotNull String status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<OrderDto.OrderResponse> page = orderService.findByUserAndStatus(userId, status, pageable);
        return ApiResponseUtil.ok(page);
    }

    // ================== GET ORDER BY ID ==================
    @Operation(
            summary = "Get order by ID",
            description = "Retrieves a single order by its ID. ADMIN can access any order, USER can access only his own orders."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOrderOwner(#id, principal.user.id)")
    public ResponseEntity<ApiResponse<OrderDto.OrderResponse>> getById(
            @Parameter(description = "ID of the order to retrieve", required = true, example = "1")
            @PathVariable("id") @NotNull @Min(1) Long id
    ) {
        return ApiResponseUtil.ok(orderService.findById(id));
    }

    // ================== UPDATE ORDER STATUS (ADMIN ONLY) ==================
    @Operation(
            summary = "Update order status (ADMIN only)",
            description = "Updates the status of an order by its ID. Requires ADMIN role."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Order status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status value or order not updatable",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @Parameter(description = "ID of the order to update", required = true, example = "1")
            @PathVariable("id") @NotNull @Min(1) Long id,
            @Parameter(description = "New status for the order", required = true, example = "SHIPPED")
            @RequestParam("status") @NotNull String status
    ) {
        orderService.updateStatus(id, status);
        return ApiResponseUtil.noContent();
    }
}
