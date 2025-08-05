package com.veggieshop.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserDto.UserResponse register(UserDto.UserCreateRequest request);
    UserDto.UserResponse update(Long id, UserDto.UserUpdateRequest request);
    void delete(Long id);
    UserDto.UserResponse findById(Long id);

    // Paging and sorting for all users (ADMIN only)
    Page<UserDto.UserResponse> findAll(Pageable pageable);

    // Find users by role (ADMIN, USER, etc.)
    Page<UserDto.UserResponse> findByRole(User.Role role, Pageable pageable);

    // Search users by name or email
    Page<UserDto.UserResponse> search(String query, Pageable pageable);

    UserDto.UserResponse findByEmail(String email);
}
