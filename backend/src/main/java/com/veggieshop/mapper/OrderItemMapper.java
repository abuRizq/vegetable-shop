package com.veggieshop.mapper;

import com.veggieshop.dto.OrderItemDto;
import com.veggieshop.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    OrderItemDto.OrderItemResponse toOrderItemResponse(OrderItem item);
}
