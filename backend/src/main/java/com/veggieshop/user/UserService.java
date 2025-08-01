package com.veggieshop.service;

import com.veggieshop.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto.UserResponse register(UserDto.UserCreateRequest request);
    UserDto.UserResponse update(Long id, UserDto.UserUpdateRequest request);
    void delete(Long id);
    UserDto.UserResponse findById(Long id);
    List<UserDto.UserResponse> findAll();
    UserDto.UserResponse findByEmail(String email);
}
