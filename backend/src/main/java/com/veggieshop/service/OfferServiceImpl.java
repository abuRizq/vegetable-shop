package com.veggieshop.service;

import com.veggieshop.dto.OfferDto;
import com.veggieshop.entity.Offer;
import com.veggieshop.entity.Product;
import com.veggieshop.repository.OfferRepository;
import com.veggieshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final ProductRepository productRepository;

    @Override
    public OfferDto.OfferResponse create(OfferDto.OfferCreateRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Offer offer = Offer.builder()
                .product(product)
                .discount(request.getDiscount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        Offer saved = offerRepository.save(offer);
        return mapToResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!offerRepository.existsById(id)) {
            throw new RuntimeException("Offer not found");
        }
        offerRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public OfferDto.OfferResponse findById(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        return mapToResponse(offer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfferDto.OfferResponse> findAll() {
        return offerRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfferDto.OfferResponse> findByProduct(Long productId) {
        return offerRepository.findByProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OfferDto.OfferResponse mapToResponse(Offer offer) {
        OfferDto.OfferResponse dto = new OfferDto.OfferResponse();
        dto.setId(offer.getId());
        dto.setProductId(offer.getProduct().getId());
        dto.setProductName(offer.getProduct().getName());
        dto.setDiscount(offer.getDiscount());
        dto.setStartDate(offer.getStartDate());
        dto.setEndDate(offer.getEndDate());
        return dto;
    }
}
