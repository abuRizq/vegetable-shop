package com.veggieshop.product;

import com.veggieshop.common.ApiResponse;
import com.veggieshop.common.ApiError;
import com.veggieshop.common.Meta;
import com.veggieshop.common.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Controller", description = "APIs for managing products in the store")
public class ProductController {

    private final ProductService productService;

    // ================== GET ALL PRODUCTS (PAGINATED & SORTABLE) ==================
    @Operation(
            summary = "Get all products (paginated and sortable)",
            description = "Retrieves a paged list of all active products. Supports query params: page (0), size (20), sort (e.g. sort=name,asc)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged list of products",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDto.ProductResponse>>> getAll(Pageable pageable) {
        Page<ProductDto.ProductResponse> page = productService.findAll(pageable);
        return ApiResponseUtil.ok(page);
    }

    // ================== GET PRODUCT BY ID ==================
    @Operation(
            summary = "Get product by ID",
            description = "Retrieves a product by its unique identifier."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto.ProductResponse>> getById(
            @Parameter(description = "ID of the product", required = true, example = "1")
            @PathVariable("id") @NotNull @Min(1) Long id
    ) {
        return ApiResponseUtil.ok(productService.findById(id));
    }

    // ================== GET FEATURED PRODUCTS ==================
    @Operation(
            summary = "Get featured products (paginated and sortable)",
            description = "Retrieves a paged list of featured active products."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged list of featured products",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class)))
    })
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProductDto.ProductResponse>>> getFeatured(Pageable pageable) {
        return ApiResponseUtil.ok(productService.findFeatured(pageable));
    }

    // ================== GET PRODUCTS BY CATEGORY ==================
    @Operation(
            summary = "Get products by category (paginated and sortable)",
            description = "Retrieves paged products belonging to a specific category."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged list of category products",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductDto.ProductResponse>>> getByCategory(
            @Parameter(description = "ID of the category", required = true, example = "2")
            @PathVariable("categoryId") @NotNull @Min(1) Long categoryId,
            Pageable pageable
    ) {
        return ApiResponseUtil.ok(productService.findByCategory(categoryId, pageable));
    }

    // ================== SEARCH PRODUCTS BY NAME ==================
    @Operation(
            summary = "Search products by name (paginated and sortable)",
            description = "Finds active products by name substring (case-insensitive, paginated, sortable)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged search results",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDto.ProductResponse>>> searchByName(
            @Parameter(description = "Substring to search in product name", required = true)
            @RequestParam("name") @NotBlank String name,
            Pageable pageable
    ) {
        return ApiResponseUtil.ok(productService.searchByName(name, pageable));
    }

    // ================== FILTER PRODUCTS BY PRICE RANGE ==================
    @Operation(
            summary = "Filter products by price (paginated and sortable)",
            description = "Finds active products within a price range (inclusive, paginated, sortable)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged filtered results",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid price range",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<ProductDto.ProductResponse>>> filterByPrice(
            @Parameter(description = "Minimum price (inclusive)", required = true, example = "1.00")
            @RequestParam("min") @NotNull @Min(0) BigDecimal min,
            @Parameter(description = "Maximum price (inclusive)", required = true, example = "100.00")
            @RequestParam("max") @NotNull @Min(0) BigDecimal max,
            Pageable pageable
    ) {
        return ApiResponseUtil.ok(productService.filterByPrice(min, max, pageable));
    }

    // ================== CREATE NEW PRODUCT ==================
    @Operation(
            summary = "Create a new product",
            description = "Creates a new product. Requires ADMIN role."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Product created",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate resource",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDto.ProductResponse>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product creation data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProductDto.ProductCreateRequest.class))
            )
            @RequestBody @Valid ProductDto.ProductCreateRequest request
    ) {
        ProductDto.ProductResponse created = productService.create(request);
        return ApiResponseUtil.created(created);
    }

    // ================== UPDATE EXISTING PRODUCT ==================
    @Operation(
            summary = "Update an existing product",
            description = "Updates a product by its ID. Requires ADMIN role."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product updated",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDto.ProductResponse>> update(
            @Parameter(description = "ID of the product to update", required = true, example = "1")
            @PathVariable("id") @NotNull @Min(1) Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product update data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProductDto.ProductUpdateRequest.class))
            )
            @RequestBody @Valid ProductDto.ProductUpdateRequest request
    ) {
        ProductDto.ProductResponse updated = productService.update(id, request);
        return ApiResponseUtil.ok(updated);
    }

    // ================== DELETE PRODUCT ==================
    @Operation(
            summary = "Delete a product",
            description = "Deletes a product by its ID. Requires ADMIN role. (Soft delete if product has orders)"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Product deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID of the product to delete", required = true, example = "1")
            @PathVariable("id") @NotNull @Min(1) Long id
    ) {
        productService.delete(id);
        return ApiResponseUtil.noContent();
    }
}
