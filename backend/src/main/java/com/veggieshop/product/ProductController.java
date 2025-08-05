package com.veggieshop.product;

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

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Controller", description = "APIs for managing products")
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "Get all products (paginated)",
            description = "Retrieves a paged list of all active products."
    )
    @GetMapping
    public ResponseEntity<Page<ProductDto.ProductResponse>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(productService.findAll(pageable));
    }

    @Operation(
            summary = "Get product by ID",
            description = "Retrieves a product by its unique identifier."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto.ProductResponse> getById(
            @Parameter(description = "ID of the product to retrieve", required = true, example = "1")
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @Operation(
            summary = "Get featured products (paginated)",
            description = "Retrieves a paged list of featured active products."
    )
    @GetMapping("/featured")
    public ResponseEntity<Page<ProductDto.ProductResponse>> getFeatured(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(productService.findFeatured(pageable));
    }

    @Operation(
            summary = "Get products by category (paginated)",
            description = "Retrieves paged products belonging to a specific category."
    )
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductDto.ProductResponse>> getByCategory(
            @Parameter(description = "ID of the category", required = true, example = "2")
            @PathVariable("categoryId") Long categoryId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(productService.findByCategory(categoryId, pageable));
    }

    @Operation(
            summary = "Search products by name (paginated)",
            description = "Find active products by name substring (case-insensitive, paginated)."
    )
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto.ProductResponse>> searchByName(
            @RequestParam("name") String name,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(productService.searchByName(name, pageable));
    }

    @Operation(
            summary = "Filter products by price (paginated)",
            description = "Find active products within a price range (inclusive, paginated)."
    )
    @GetMapping("/filter")
    public ResponseEntity<Page<ProductDto.ProductResponse>> filterByPrice(
            @RequestParam("min") BigDecimal min,
            @RequestParam("max") BigDecimal max,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(productService.filterByPrice(min, max, pageable));
    }

    @Operation(
            summary = "Create a new product",
            description = "Creates a new product. Requires ADMIN role."
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto.ProductResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product creation data",
                    required = true
            )
            @RequestBody @Valid ProductDto.ProductCreateRequest request) {
        return ResponseEntity.ok(productService.create(request));
    }

    @Operation(
            summary = "Update an existing product",
            description = "Updates a product by its ID. Requires ADMIN role."
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto.ProductResponse> update(
            @Parameter(description = "ID of the product to update", required = true, example = "1")
            @PathVariable("id") Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product update data",
                    required = true
            )
            @RequestBody @Valid ProductDto.ProductUpdateRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @Operation(
            summary = "Delete a product",
            description = "Deletes a product by its ID. Requires ADMIN role.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Product not found")
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the product to delete", required = true, example = "1")
            @PathVariable("id") Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
