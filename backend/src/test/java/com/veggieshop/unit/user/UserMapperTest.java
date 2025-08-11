package com.veggieshop.unit.user;

import com.veggieshop.user.User;
import com.veggieshop.user.dto.UserDto;
import com.veggieshop.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        // MapStruct mapper instance
        userMapper = Mappers.getMapper(UserMapper.class);
    }

    @Test
    void toUserResponse_shouldMapAllFieldsCorrectly() {
        // Arrange
        User user = User.builder()
                .id(10L)
                .name("Alice Example")
                .email("alice@example.com")
                .password("encodedpassword")
                .role(User.Role.ADMIN)
                .enabled(true)
                .createdAt(Instant.parse("2024-08-01T12:00:00Z"))
                .updatedAt(Instant.parse("2024-08-02T10:30:00Z"))
                .build();

        // Act
        UserDto.UserResponse dto = userMapper.toUserResponse(user);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(user.getId());
        assertThat(dto.getName()).isEqualTo(user.getName());
        assertThat(dto.getEmail()).isEqualTo(user.getEmail());
        assertThat(dto.getRole()).isEqualTo(user.getRole());
        // Note: Password, enabled, createdAt, updatedAt are not mapped (by design)
    }

    @Test
    void toUserResponse_shouldReturnNull_whenUserIsNull() {
        // Act & Assert
        assertThat(userMapper.toUserResponse(null)).isNull();
    }

    @Test
    void toUserResponseList_shouldMapListCorrectly() {
        // Arrange
        User user1 = User.builder()
                .id(1L)
                .name("Admin")
                .email("admin@veggieshop.com")
                .role(User.Role.ADMIN)
                .build();
        User user2 = User.builder()
                .id(2L)
                .name("User")
                .email("user@veggieshop.com")
                .role(User.Role.USER)
                .build();
        List<User> users = Arrays.asList(user1, user2);

        // Act
        List<UserDto.UserResponse> responses = userMapper.toUserResponseList(users);

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(user1.getId());
        assertThat(responses.get(1).getId()).isEqualTo(user2.getId());
        assertThat(responses.get(0).getName()).isEqualTo("Admin");
        assertThat(responses.get(1).getEmail()).isEqualTo("user@veggieshop.com");
    }

    @Test
    void toUserResponseList_shouldReturnEmpty_whenInputListIsEmpty() {
        // Act
        List<UserDto.UserResponse> responses = userMapper.toUserResponseList(List.of());

        // Assert
        assertThat(responses).isEmpty();
    }

    @Test
    void toUserResponseList_shouldReturnNull_whenInputListIsNull() {
        // Act
        List<UserDto.UserResponse> responses = userMapper.toUserResponseList(null);

        // Assert
        assertThat(responses).isNull();
    }
}
