package com.veggieshop.offer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    Page<Offer> findAll(Pageable pageable);

    Page<Offer> findByProductId(Long productId, Pageable pageable);

    // Example: Filtering by active offers for today
    Page<Offer> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            java.time.LocalDate start, java.time.LocalDate end, Pageable pageable
    );
}
