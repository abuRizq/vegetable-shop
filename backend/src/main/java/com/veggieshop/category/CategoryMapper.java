package com.veggieshop.category;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto.CategoryResponse toCategoryResponse(Category category);
}
