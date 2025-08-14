package com.veggieshop.unit.user;

import com.veggieshop.exception.BadRequestException;
import com.veggieshop.exception.DuplicateException;
import com.veggieshop.exception.ResourceNotFoundException;
import com.veggieshop.user.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // =========== Registration ===========
    @Test
    void register_shouldSucceed_whenDataIsValid() {
        // Arrange
        UserDto.UserCreateRequest req = new UserDto.UserCreateRequest();
        req.setName("Test User");
        req.setEmail("test@demo.com");
        req.setPassword("password");
        req.setRole(User.Role.USER);

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(req.getPassword())).thenReturn("hashed_pw");
        User user = User.builder()
                .id(1L)
                .name(req.getName())
                .email(req.getEmail())
                .password("hashed_pw")
                .role(User.Role.USER)
                .createdAt(Instant.now())
                .enabled(true)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto.UserResponse resp = new UserDto.UserResponse();
        resp.setId(user.getId());
        resp.setName(user.getName());
        resp.setEmail(user.getEmail());
        resp.setRole(user.getRole());
        when(userMapper.toUserResponse(any(User.class))).thenReturn(resp);

        // Act
        UserDto.UserResponse result = userService.register(req);

        // Assert
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getEmail()).isEqualTo("test@demo.com");
        assertThat(result.getRole()).isEqualTo(User.Role.USER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowDuplicateException_whenEmailExists() {
        UserDto.UserCreateRequest req = new UserDto.UserCreateRequest();
        req.setEmail("test@demo.com");
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

        assertThrows(DuplicateException.class, () -> userService.register(req));
        verify(userRepository, never()).save(any());
    }

    // =========== Update ===========
    @Test
    void update_shouldSucceed_whenUserExists() {
        Long userId = 1L;
        User existing = User.builder().id(userId).name("Old").email("old@demo.com").build();
        UserDto.UserUpdateRequest req = new UserDto.UserUpdateRequest();
        req.setName("Updated");
        req.setEmail("updated@demo.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
        User updated = User.builder().id(userId).name("Updated").email("updated@demo.com").build();
        when(userRepository.save(existing)).thenReturn(updated);

        UserDto.UserResponse resp = new UserDto.UserResponse();
        resp.setId(userId); resp.setName("Updated"); resp.setEmail("updated@demo.com");
        when(userMapper.toUserResponse(updated)).thenReturn(resp);

        UserDto.UserResponse result = userService.update(userId, req);

        assertThat(result.getName()).isEqualTo("Updated");
        assertThat(result.getEmail()).isEqualTo("updated@demo.com");
        verify(userRepository).save(existing);
    }

    @Test
    void update_shouldThrowNotFound_whenUserMissing() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        UserDto.UserUpdateRequest req = new UserDto.UserUpdateRequest();
        assertThrows(ResourceNotFoundException.class, () -> userService.update(userId, req));
    }

    // =========== Delete ===========
    @Test
    void delete_shouldSucceed_whenUserExists() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        userService.delete(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void delete_shouldThrowNotFound_whenUserMissing() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> userService.delete(userId));
    }

    // =========== Find By Id ===========
    @Test
    void findById_shouldReturnUser_whenFound() {
        Long userId = 1L;
        User user = User.builder().id(userId).name("User").email("u@d.com").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UserDto.UserResponse resp = new UserDto.UserResponse();
        resp.setId(userId); resp.setName("User"); resp.setEmail("u@d.com");
        when(userMapper.toUserResponse(user)).thenReturn(resp);

        UserDto.UserResponse result = userService.findById(userId);
        assertThat(result.getName()).isEqualTo("User");
    }

    @Test
    void findById_shouldThrowNotFound_whenMissing() {
        Long userId = 5L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.findById(userId));
    }

    // =========== Find All ===========
    @Test
    void findAll_shouldReturnPagedUsers() {
        Pageable pageable = PageRequest.of(0, 2);
        User u1 = User.builder().id(1L).name("A").email("a@d.com").build();
        User u2 = User.builder().id(2L).name("B").email("b@d.com").build();
        List<User> users = List.of(u1, u2);

        Page<User> userPage = new PageImpl<>(users, pageable, 2);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        UserDto.UserResponse r1 = new UserDto.UserResponse();
        r1.setId(1L); r1.setName("A"); r1.setEmail("a@d.com");
        UserDto.UserResponse r2 = new UserDto.UserResponse();
        r2.setId(2L); r2.setName("B"); r2.setEmail("b@d.com");

        when(userMapper.toUserResponse(u1)).thenReturn(r1);
        when(userMapper.toUserResponse(u2)).thenReturn(r2);

        Page<UserDto.UserResponse> result = userService.findAll(pageable);
        assertThat(result.getContent()).hasSize(2);
    }

    // =========== Find By Role ===========
    @Test
    void findByRole_shouldReturnPagedAdmins() {
        Pageable pageable = PageRequest.of(0, 1);
        User u1 = User.builder().id(1L).name("Admin").email("a@d.com").role(User.Role.ADMIN).build();
        Page<User> userPage = new PageImpl<>(List.of(u1), pageable, 1);
        when(userRepository.findByRole(User.Role.ADMIN, pageable)).thenReturn(userPage);

        UserDto.UserResponse r1 = new UserDto.UserResponse();
        r1.setId(1L); r1.setName("Admin"); r1.setRole(User.Role.ADMIN);
        when(userMapper.toUserResponse(u1)).thenReturn(r1);

        Page<UserDto.UserResponse> result = userService.findByRole(User.Role.ADMIN, pageable);
        assertThat(result.getContent()).hasSize(1)
                .first().hasFieldOrPropertyWithValue("role", User.Role.ADMIN);
    }

    // =========== Search ===========
    @Test
    void search_shouldReturnMatchedUsers() {
        Pageable pageable = PageRequest.of(0, 2);
        User u1 = User.builder().id(1L).name("A").email("a@d.com").build();
        User u2 = User.builder().id(2L).name("AB").email("ab@d.com").build();
        Page<User> userPage = new PageImpl<>(List.of(u1, u2), pageable, 2);

        when(userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase("a", "a", pageable))
                .thenReturn(userPage);
        when(userMapper.toUserResponse(u1)).thenReturn(new UserDto.UserResponse());
        when(userMapper.toUserResponse(u2)).thenReturn(new UserDto.UserResponse());

        Page<UserDto.UserResponse> result = userService.search("a", pageable);
        assertThat(result.getContent()).hasSize(2);
    }

    // =========== Find By Email ===========
    @Test
    void findByEmail_shouldReturnUser_whenExists() {
        User user = User.builder().id(1L).email("user@x.com").build();
        when(userRepository.findByEmail("user@x.com")).thenReturn(Optional.of(user));
        UserDto.UserResponse resp = new UserDto.UserResponse();
        resp.setId(1L); resp.setEmail("user@x.com");
        when(userMapper.toUserResponse(user)).thenReturn(resp);

        UserDto.UserResponse result = userService.findByEmail("user@x.com");
        assertThat(result.getEmail()).isEqualTo("user@x.com");
    }

    @Test
    void findByEmail_shouldThrowNotFound_whenMissing() {
        when(userRepository.findByEmail("nobody@x.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.findByEmail("nobody@x.com"));
    }

    // =========== Change Password ===========
    @Test
    void changePassword_shouldSucceed_whenOldPasswordMatches() {
        Long userId = 1L;
        User user = User.builder().id(userId).password("hashed_old").build();
        UserDto.PasswordChangeRequest req = new UserDto.PasswordChangeRequest();
        req.setOldPassword("oldPass");
        req.setNewPassword("newPass");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "hashed_old")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("hashed_new");
        when(userRepository.save(user)).thenReturn(user);

        userService.changePassword(userId, req);

        verify(userRepository).save(user);
        assertThat(user.getPassword()).isEqualTo("hashed_new");
    }

    @Test
    void changePassword_shouldThrow_whenOldPasswordWrong() {
        Long userId = 1L;
        User user = User.builder().id(userId).password("hashed_old").build();
        UserDto.PasswordChangeRequest req = new UserDto.PasswordChangeRequest();
        req.setOldPassword("wrongOld");
        req.setNewPassword("newPass");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOld", "hashed_old")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> userService.changePassword(userId, req));
    }

    // =========== Change Role ===========
    @Test
    void changeRole_shouldUpdateRole() {
        Long userId = 1L;
        User user = User.builder().id(userId).role(User.Role.USER).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserDto.UserResponse resp = new UserDto.UserResponse();
        resp.setId(userId); resp.setRole(User.Role.ADMIN);
        when(userMapper.toUserResponse(user)).thenReturn(resp);

        UserDto.UserResponse result = userService.changeRole(userId, User.Role.ADMIN);

        assertThat(result.getRole()).isEqualTo(User.Role.ADMIN);
        assertThat(user.getRole()).isEqualTo(User.Role.ADMIN);
    }

    @Test
    void changeRole_shouldThrowNotFound_whenUserMissing() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.changeRole(userId, User.Role.ADMIN));
    }
}
