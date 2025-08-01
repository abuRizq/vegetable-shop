package com.veggieshop.service;

import com.veggieshop.dto.OfferDto;

import java.util.List;

public interface OfferService {
    OfferDto.OfferResponse create(OfferDto.OfferCreateRequest request);
    void delete(Long id);
    OfferDto.OfferResponse findById(Long id);
    List<OfferDto.OfferResponse> findAll();
    List<OfferDto.OfferResponse> findByProduct(Long productId);
}
