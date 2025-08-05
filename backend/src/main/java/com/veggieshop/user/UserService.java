package com.veggieshop.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserDto.UserResponse register(UserDto.UserCreateRequest request);

    UserDto.UserResponse update(Long id, UserDto.UserUpdateRequest request);

    void delete(Long id);

    UserDto.UserResponse findById(Long id);

    Page<UserDto.UserResponse> findAll(Pageable pageable);

    Page<UserDto.UserResponse> findByRole(User.Role role, Pageable pageable);

    Page<UserDto.UserResponse> search(String query, Pageable pageable);

    UserDto.UserResponse findByEmail(String email);

    void changePassword(Long userId, UserDto.PasswordChangeRequest request);

    UserDto.UserResponse changeRole(Long userId, User.Role newRole);
}
