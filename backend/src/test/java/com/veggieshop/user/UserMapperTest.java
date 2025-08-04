package com.veggieshop.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
    }

    @Test
    @DisplayName("should map User to UserResponse correctly")
    void toUserResponse_shouldMapAllFields() {
        User user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@domain.com")
                .role(User.Role.USER)
                .build();

        UserDto.UserResponse dto = userMapper.toUserResponse(user);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Test User");
        assertThat(dto.getEmail()).isEqualTo("test@domain.com");
        assertThat(dto.getRole()).isEqualTo(User.Role.USER);
    }

    @Test
    @DisplayName("should map list of User to list of UserResponse")
    void toUserResponseList_shouldMapList() {
        User user1 = User.builder()
                .id(1L)
                .name("User1")
                .email("user1@mail.com")
                .role(User.Role.USER)
                .build();
        User user2 = User.builder()
                .id(2L)
                .name("User2")
                .email("user2@mail.com")
                .role(User.Role.ADMIN)
                .build();

        List<User> users = List.of(user1, user2);

        List<UserDto.UserResponse> dtos = userMapper.toUserResponseList(users);

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getId()).isEqualTo(1L);
        assertThat(dtos.get(1).getRole()).isEqualTo(User.Role.ADMIN);
    }

    @Test
    @DisplayName("should handle null user")
    void toUserResponse_null() {
        UserDto.UserResponse dto = userMapper.toUserResponse(null);
        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("should handle null list")
    void toUserResponseList_null() {
        List<UserDto.UserResponse> dtos = userMapper.toUserResponseList(null);
        assertThat(dtos).isNull();
    }
}
