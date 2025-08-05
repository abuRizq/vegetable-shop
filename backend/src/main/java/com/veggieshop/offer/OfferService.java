package com.veggieshop.offer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OfferService {
    OfferDto.OfferResponse create(OfferDto.OfferCreateRequest request);
    void delete(Long id);
    OfferDto.OfferResponse findById(Long id);

    // Pagination & Sorting
    Page<OfferDto.OfferResponse> findAll(Pageable pageable);

    // By Product with Paging
    Page<OfferDto.OfferResponse> findByProduct(Long productId, Pageable pageable);

    // Example: Active offers (current date within offer period)
    Page<OfferDto.OfferResponse> findActiveOffers(Pageable pageable);
}
