package com.veggieshop.offer;

import com.veggieshop.product.Product;
import com.veggieshop.exception.ResourceNotFoundException;
import com.veggieshop.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final ProductRepository productRepository;
    private final OfferMapper offerMapper;

    @Override
    public OfferDto.OfferResponse create(OfferDto.OfferCreateRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Offer offer = Offer.builder()
                .product(product)
                .discount(request.getDiscount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        Offer saved = offerRepository.save(offer);
        return offerMapper.toOfferResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!offerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Offer not found");
        }
        offerRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public OfferDto.OfferResponse findById(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));
        return offerMapper.toOfferResponse(offer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OfferDto.OfferResponse> findAll(Pageable pageable) {
        return offerRepository.findAll(pageable)
                .map(offerMapper::toOfferResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OfferDto.OfferResponse> findByProduct(Long productId, Pageable pageable) {
        return offerRepository.findByProductId(productId, pageable)
                .map(offerMapper::toOfferResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OfferDto.OfferResponse> findActiveOffers(Pageable pageable) {
        LocalDate today = LocalDate.now();
        return offerRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                today, today, pageable
        ).map(offerMapper::toOfferResponse);
    }
}
