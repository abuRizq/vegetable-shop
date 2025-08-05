package com.veggieshop.offer;

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
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@Validated
@Tag(name = "Offer Controller", description = "APIs for managing offers and discounts")
public class OfferController {

    private final OfferService offerService;

    @Operation(
            summary = "Get all offers (paged)",
            description = "Retrieves a paged list of offers. Supports sorting and filtering."
    )
    @GetMapping
    public ResponseEntity<Page<OfferDto.OfferResponse>> getAllPaged(
            @PageableDefault(size = 20, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(offerService.findAll(pageable));
    }

    @Operation(
            summary = "Get offer by ID",
            description = "Retrieves an offer by its unique identifier."
    )
    @GetMapping("/{id}")
    public ResponseEntity<OfferDto.OfferResponse> getById(
            @Parameter(description = "ID of the offer to retrieve", required = true, example = "1")
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(offerService.findById(id));
    }

    @Operation(
            summary = "Get offers by product (paged)",
            description = "Retrieves offers related to a specific product with pagination and sorting."
    )
    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<OfferDto.OfferResponse>> getByProductPaged(
            @Parameter(description = "ID of the product", required = true, example = "1")
            @PathVariable("productId") Long productId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(offerService.findByProduct(productId, pageable));
    }

    @Operation(
            summary = "Get active offers (paged)",
            description = "Retrieves only currently active offers."
    )
    @GetMapping("/active")
    public ResponseEntity<Page<OfferDto.OfferResponse>> getActiveOffers(
            @PageableDefault(size = 10, sort = "startDate") Pageable pageable
    ) {
        return ResponseEntity.ok(offerService.findActiveOffers(pageable));
    }

    @Operation(
            summary = "Create a new offer",
            description = "Creates a new offer. Requires ADMIN role."
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OfferDto.OfferResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Offer creation data",
                    required = true
            )
            @RequestBody @Valid OfferDto.OfferCreateRequest request) {
        return ResponseEntity.ok(offerService.create(request));
    }

    @Operation(
            summary = "Delete an offer",
            description = "Deletes an offer by its ID. Requires ADMIN role.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Offer deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Offer not found")
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the offer to delete", required = true, example = "4")
            @PathVariable("id") Long id) {
        offerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
