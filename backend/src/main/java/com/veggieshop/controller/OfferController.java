package com.veggieshop.controller;

import com.veggieshop.dto.OfferDto;
import com.veggieshop.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@Validated
public class OfferController {

    private final OfferService offerService;

    @GetMapping
    public ResponseEntity<List<OfferDto.OfferResponse>> getAll() {
        return ResponseEntity.ok(offerService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferDto.OfferResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.findById(id));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<OfferDto.OfferResponse>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(offerService.findByProduct(productId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OfferDto.OfferResponse> create(@RequestBody @Valid OfferDto.OfferCreateRequest request) {
        return ResponseEntity.ok(offerService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        offerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
