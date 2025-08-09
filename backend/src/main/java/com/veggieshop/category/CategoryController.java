package com.veggieshop.category;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
@Tag(name = "Category Controller", description = "APIs for managing categories")
public class CategoryController {

    private final CategoryService categoryService;

    // ================== GET ALL CATEGORIES (PAGED) ==================
    @Operation(
            summary = "Get all categories (paged)",
            description = "Retrieves a paged list of categories. Supports sorting and filtering."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged list of categories",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDto.CategoryResponse>>> getAllPaged(
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        Page<CategoryDto.CategoryResponse> page = categoryService.findAll(pageable);
        return ApiResponseUtil.ok(page);
    }

    // ================== SEARCH CATEGORIES BY NAME (PAGED) ==================
    @Operation(
            summary = "Search categories by name (paged)",
            description = "Searches categories by partial name match. Supports pagination and sorting."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged search results",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CategoryDto.CategoryResponse>>> searchByName(
            @Parameter(description = "Partial name to search for", name = "name", required = true)
            @RequestParam(name = "name") @NotBlank String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<CategoryDto.CategoryResponse> page = categoryService.searchByName(name, pageable);
        return ApiResponseUtil.ok(page);
    }

    // ================== GET CATEGORY BY ID ==================
    @Operation(
            summary = "Get category by ID",
            description = "Retrieves a category by its unique identifier."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDto.CategoryResponse>> getById(
            @Parameter(description = "ID of the category to retrieve", required = true, example = "1")
            @PathVariable("id") @NotNull @Min(1) Long id
    ) {
        return ApiResponseUtil.ok(categoryService.findById(id));
    }

    // ================== CREATE NEW CATEGORY ==================
    @Operation(
            summary = "Create a new category",
            description = "Creates a new category. Requires ADMIN role."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Category created",
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
    public ResponseEntity<ApiResponse<CategoryDto.CategoryResponse>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Category creation data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CategoryDto.CategoryCreateRequest.class))
            )
            @RequestBody @Valid CategoryDto.CategoryCreateRequest request
    ) {
        CategoryDto.CategoryResponse created = categoryService.create(request);
        return ApiResponseUtil.created(created);
    }

    // ================== UPDATE CATEGORY ==================
    @Operation(
            summary = "Update a category",
            description = "Updates a category by its ID. Requires ADMIN role."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category updated",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryDto.CategoryResponse>> update(
            @Parameter(description = "ID of the category to update", required = true, example = "1")
            @PathVariable("id") @NotNull @Min(1) Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Category update data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CategoryDto.CategoryUpdateRequest.class))
            )
            @RequestBody @Valid CategoryDto.CategoryUpdateRequest request
    ) {
        CategoryDto.CategoryResponse updated = categoryService.update(id, request);
        return ApiResponseUtil.ok(updated);
    }

    // ================== DELETE CATEGORY ==================
    @Operation(
            summary = "Delete a category",
            description = "Deletes a category by its ID. Requires ADMIN role. Cannot delete if there are products in the category."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot delete a category with associated products",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID of the category to delete", required = true, example = "1")
            @PathVariable("id") @NotNull @Min(1) Long id
    ) {
        categoryService.delete(id);
        return ApiResponseUtil.noContent();
    }
}
