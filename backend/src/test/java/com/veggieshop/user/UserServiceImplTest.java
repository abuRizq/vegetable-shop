package com.veggieshop.user;

import com.veggieshop.exception.BadRequestException;
import com.veggieshop.exception.DuplicateException;
import com.veggieshop.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    User mockUser(Long id) {
        return User.builder()
                .id(id)
                .name("John")
                .email("john@email.com")
                .password("hashed")
                .role(User.Role.USER)
                .build();
    }

    UserDto.UserResponse mockResponse(Long id) {
        UserDto.UserResponse resp = new UserDto.UserResponse();
        resp.setId(id);
        resp.setName("John");
        resp.setEmail("john@email.com");
        resp.setRole(User.Role.USER);
        return resp;
    }

    @Test
    @DisplayName("Register user - success")
    void register_success() {
        UserDto.UserCreateRequest req = new UserDto.UserCreateRequest();
        req.setName("John");
        req.setEmail("john@email.com");
        req.setPassword("123456");
        req.setRole(User.Role.USER);

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(42L);
            return u;
        });
        when(userMapper.toUserResponse(any(User.class))).thenAnswer(inv -> mockResponse(42L));

        UserDto.UserResponse resp = userService.register(req);
        assertThat(resp.getId()).isEqualTo(42L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register user - duplicate email")
    void register_duplicateEmail() {
        UserDto.UserCreateRequest req = new UserDto.UserCreateRequest();
        req.setName("John");
        req.setEmail("john@email.com");
        req.setPassword("123456");

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(req))
                .isInstanceOf(DuplicateException.class)
                .hasMessageContaining("Email already exists");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update user - success")
    void update_success() {
        Long id = 99L;
        UserDto.UserUpdateRequest req = new UserDto.UserUpdateRequest();
        req.setName("Updated");
        req.setEmail("up@email.com");

        User user = mockUser(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(mockResponse(id));

        UserDto.UserResponse resp = userService.update(id, req);

        assertThat(resp.getId()).isEqualTo(id);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Update user - not found")
    void update_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        UserDto.UserUpdateRequest req = new UserDto.UserUpdateRequest();
        req.setName("N");
        req.setEmail("E");
        assertThatThrownBy(() -> userService.update(1L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Delete user - success")
    void delete_success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.delete(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Delete user - not found")
    void delete_notFound() {
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> userService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Find by ID - found")
    void findById_found() {
        User user = mockUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(mockResponse(1L));
        UserDto.UserResponse resp = userService.findById(1L);
        assertThat(resp.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Find by ID - not found")
    void findById_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Find all - paged")
    void findAll_paged() {
        User user = mockUser(1L);
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(mockResponse(1L));
        Page<UserDto.UserResponse> page = userService.findAll(PageRequest.of(0, 1));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Find by role - paged")
    void findByRole_paged() {
        User user = mockUser(1L);
        when(userRepository.findByRole(eq(User.Role.USER), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(mockResponse(1L));
        Page<UserDto.UserResponse> page = userService.findByRole(User.Role.USER, PageRequest.of(0, 1));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Search users - paged")
    void search_paged() {
        User user = mockUser(1L);
        when(userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(mockResponse(1L));
        Page<UserDto.UserResponse> page = userService.search("j", PageRequest.of(0, 1));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Find by email - found")
    void findByEmail_found() {
        User user = mockUser(1L);
        when(userRepository.findByEmail("john@email.com")).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(mockResponse(1L));
        UserDto.UserResponse resp = userService.findByEmail("john@email.com");
        assertThat(resp.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Find by email - not found")
    void findByEmail_notFound() {
        when(userRepository.findByEmail("none@email.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.findByEmail("none@email.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Nested
    class ChangePasswordTest {
        @Test
        @DisplayName("Change password - success")
        void changePassword_success() {
            User user = mockUser(1L);
            UserDto.PasswordChangeRequest req = new UserDto.PasswordChangeRequest();
            req.setOldPassword("oldpass");
            req.setNewPassword("newpass");

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldpass", "hashed")).thenReturn(true);
            when(passwordEncoder.encode("newpass")).thenReturn("hashed2");
            when(userRepository.save(any(User.class))).thenReturn(user);

            userService.changePassword(1L, req);
            verify(userRepository).save(argThat(u -> u.getPassword().equals("hashed2")));
        }

        @Test
        @DisplayName("Change password - user not found")
        void changePassword_userNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());
            UserDto.PasswordChangeRequest req = new UserDto.PasswordChangeRequest();
            req.setOldPassword("oldpass");
            req.setNewPassword("newpass");
            assertThatThrownBy(() -> userService.changePassword(1L, req))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Change password - old password wrong")
        void changePassword_wrongOld() {
            User user = mockUser(1L);
            UserDto.PasswordChangeRequest req = new UserDto.PasswordChangeRequest();
            req.setOldPassword("wrong");
            req.setNewPassword("newpass");

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

            assertThatThrownBy(() -> userService.changePassword(1L, req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Old password is incorrect");
        }
    }

    @Nested
    class ChangeRoleTest {
        @Test
        @DisplayName("Change role - success")
        void changeRole_success() {
            User user = mockUser(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.toUserResponse(any(User.class))).thenReturn(mockResponse(1L));

            UserDto.UserResponse resp = userService.changeRole(1L, User.Role.ADMIN);
            assertThat(resp.getId()).isEqualTo(1L);
            verify(userRepository).save(argThat(u -> u.getRole() == User.Role.ADMIN));
        }

        @Test
        @DisplayName("Change role - not found")
        void changeRole_notFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> userService.changeRole(1L, User.Role.ADMIN))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
