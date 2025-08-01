package com.veggieshop.service;

import com.veggieshop.dto.OfferDto;
import com.veggieshop.entity.Offer;
import com.veggieshop.entity.Product;
import com.veggieshop.exception.ResourceNotFoundException;
import com.veggieshop.mapper.OfferMapper; // جديد
import com.veggieshop.repository.OfferRepository;
import com.veggieshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final ProductRepository productRepository;
    private final OfferMapper offerMapper; // جديد

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
    public List<OfferDto.OfferResponse> findAll() {
        return offerRepository.findAll()
                .stream()
                .map(offerMapper::toOfferResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfferDto.OfferResponse> findByProduct(Long productId) {
        return offerRepository.findByProductId(productId)
                .stream()
                .map(offerMapper::toOfferResponse)
                .toList();
    }
}
