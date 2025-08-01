package com.veggieshop.mapper;

import com.veggieshop.dto.ProductDto;
import com.veggieshop.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductDto.ProductResponse toProductResponse(Product product);
}
