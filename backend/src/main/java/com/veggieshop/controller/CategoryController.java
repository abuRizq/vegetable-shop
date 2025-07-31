package com.veggieshop.controller;

import com.veggieshop.dto.CategoryDto;
import com.veggieshop.service.CategoryService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

// === OpenAPI Annotations ===
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
@Tag(name = "Category Controller", description = "APIs for managing categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Get all categories",
            description = "Retrieves a list of all categories."
    )
    @GetMapping
    public ResponseEntity<List<CategoryDto.CategoryResponse>> getAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @Operation(
            summary = "Get category by ID",
            description = "Retrieves a category by its unique identifier."
    )
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto.CategoryResponse> getById(
            @Parameter(description = "ID of the category to retrieve", required = true, example = "1")
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @Operation(
            summary = "Create a new category",
            description = "Creates a new category. Requires ADMIN role."
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto.CategoryResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Category creation data",
                    required = true
            )
            @RequestBody @Valid CategoryDto.CategoryCreateRequest request) {
        return ResponseEntity.ok(categoryService.create(request));
    }

    @Operation(
            summary = "Update a category",
            description = "Updates a category by its ID. Requires ADMIN role."
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto.CategoryResponse> update(
            @Parameter(description = "ID of the category to update", required = true, example = "1")
            @PathVariable("id") Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Category update data",
                    required = true
            )
            @RequestBody @Valid CategoryDto.CategoryUpdateRequest request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @Operation(
            summary = "Delete a category",
            description = "Deletes a category by its ID. Requires ADMIN role. Cannot delete if there are products in the category.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
                    @ApiResponse(responseCode = "400", description = "Cannot delete a category with associated products"),
                    @ApiResponse(responseCode = "404", description = "Category not found")
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the category to delete", required = true, example = "1")
            @PathVariable("id") Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
