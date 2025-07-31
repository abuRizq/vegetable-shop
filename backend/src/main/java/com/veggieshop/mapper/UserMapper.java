package com.veggieshop.mapper;

import com.veggieshop.dto.UserDto;
import com.veggieshop.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto.UserResponse toUserResponse(User user);
    List<UserDto.UserResponse> toUserResponseList(List<User> users);  // Add this
}
