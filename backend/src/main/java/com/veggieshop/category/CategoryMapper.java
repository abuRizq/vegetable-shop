package com.veggieshop.mapper;

import com.veggieshop.dto.CategoryDto;
import com.veggieshop.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto.CategoryResponse toCategoryResponse(Category category);
}
