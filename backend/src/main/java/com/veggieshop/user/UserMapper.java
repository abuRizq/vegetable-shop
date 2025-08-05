package com.veggieshop.user;

import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto.UserResponse toUserResponse(User user);

    List<UserDto.UserResponse> toUserResponseList(List<User> users);

    // Add more mappings if new DTOs are introduced
}
