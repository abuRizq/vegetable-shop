package com.veggieshop.mapper;

import com.veggieshop.dto.OfferDto;
import com.veggieshop.entity.Offer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OfferMapper {
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    OfferDto.OfferResponse toOfferResponse(Offer offer);
}
