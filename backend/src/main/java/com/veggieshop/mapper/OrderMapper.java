package com.veggieshop.mapper;

import com.veggieshop.dto.OrderDto;
import com.veggieshop.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "orderItems", target = "items")
    OrderDto.OrderResponse toOrderResponse(Order order);
}
