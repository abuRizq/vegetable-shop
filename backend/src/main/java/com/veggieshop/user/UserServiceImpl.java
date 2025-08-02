package com.veggieshop.user;

import com.veggieshop.exception.DuplicateException;
import com.veggieshop.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserDto.UserResponse register(UserDto.UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateException("Email already exists");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : User.Role.USER)
                .build();
        User saved = userRepository.save(user);
        return userMapper.toUserResponse(saved);
    }

    @Override
    public UserDto.UserResponse update(Long id, UserDto.UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        User updated = userRepository.save(user);
        return userMapper.toUserResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto.UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto.UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto.UserResponse> findByRole(User.Role role, Pageable pageable) {
        return userRepository.findByRole(role, pageable)
                .map(userMapper::toUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto.UserResponse> search(String query, Pageable pageable) {
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query, pageable)
                .map(userMapper::toUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto.UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toUserResponse(user);
    }
}
