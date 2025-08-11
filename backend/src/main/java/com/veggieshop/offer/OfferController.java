package com.veggieshop.offer;

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
import jakarta.validation.constraints.Min;
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
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@Validated
@Tag(name = "Offer Controller", description = "APIs for managing offers and discounts")
public class OfferController {

    private final OfferService offerService;

    // ================== GET ALL OFFERS (PAGED) ==================
    @Operation(
            summary = "Get all offers (paged)",
            description = "Retrieves a paged list of offers. Supports sorting and filtering."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged list of offers",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<OfferDto.OfferResponse>>> getAllPaged(
            @PageableDefault(size = 20, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<OfferDto.OfferResponse> page = offerService.findAll(pageable);
        return ApiResponseUtil.ok(page);
    }

    // ================== GET OFFER BY ID ==================
    @Operation(
            summary = "Get offer by ID",
            description = "Retrieves an offer by its unique identifier."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Offer found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Offer not found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OfferDto.OfferResponse>> getById(
            @Parameter(description = "ID of the offer to retrieve", required = true, example = "1")
            @PathVariable("id") @NotNull @Min(1) Long id
    ) {
        return ApiResponseUtil.ok(offerService.findById(id));
    }

    // ================== GET OFFERS BY PRODUCT (PAGED) ==================
    @Operation(
            summary = "Get offers by product (paged)",
            description = "Retrieves offers related to a specific product with pagination and sorting."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged list of offers for product",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<OfferDto.OfferResponse>>> getByProductPaged(
            @Parameter(description = "ID of the product", required = true, example = "1")
            @PathVariable("productId") @NotNull @Min(1) Long productId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<OfferDto.OfferResponse> page = offerService.findByProduct(productId, pageable);
        return ApiResponseUtil.ok(page);
    }

    // ================== GET ACTIVE OFFERS (PAGED) ==================
    @Operation(
            summary = "Get active offers (paged)",
            description = "Retrieves only currently active offers."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged list of active offers",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiResponse.class)))
    })
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<OfferDto.OfferResponse>>> getActiveOffers(
            @PageableDefault(size = 10, sort = "startDate") Pageable pageable
    ) {
        Page<OfferDto.OfferResponse> page = offerService.findActiveOffers(pageable);
        return ApiResponseUtil.ok(page);
    }

    // ================== CREATE NEW OFFER ==================
    @Operation(
            summary = "Create a new offer",
            description = "Creates a new offer. Requires ADMIN role."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Offer created",
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
    public ResponseEntity<ApiResponse<OfferDto.OfferResponse>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Offer creation data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = OfferDto.OfferCreateRequest.class))
            )
            @RequestBody @Valid OfferDto.OfferCreateRequest request
    ) {
        OfferDto.OfferResponse created = offerService.create(request);
        return ApiResponseUtil.created(created);
    }

    // ================== DELETE OFFER ==================
    @Operation(
            summary = "Delete an offer",
            description = "Deletes an offer by its ID. Requires ADMIN role."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Offer deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Offer not found",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = com.veggieshop.common.ApiError.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID of the offer to delete", required = true, example = "4")
            @PathVariable("id") @NotNull @Min(1) Long id
    ) {
        offerService.delete(id);
        return ApiResponseUtil.noContent();
    }
}
