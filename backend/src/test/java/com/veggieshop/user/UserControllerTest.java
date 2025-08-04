package com.veggieshop.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veggieshop.common.ApiResponse;
import com.veggieshop.security.CustomUserDetails;
import com.veggieshop.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;


    @MockBean
    private UserService userService;

    // Helpers
    UserDto.UserResponse getUser(Long id, String name, String email, User.Role role) {
        UserDto.UserResponse user = new UserDto.UserResponse();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    @Nested
    @DisplayName("Get Current User Profile")
    class GetMe {

        @Test
        @WithMockUser(username = "john@doe.com", roles = {"USER"})
        void shouldReturnCurrentUserProfile() throws Exception {
            var user = getUser(1L, "John Doe", "john@doe.com", User.Role.USER);
            Mockito.when(userService.findById(anyLong())).thenReturn(user);

            mockMvc.perform(get("/api/users/me")
                            .with(user("john@doe.com").password("pass").roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.email").value("john@doe.com"))
                    .andExpect(jsonPath("$.data.name").value("John Doe"))
                    .andExpect(jsonPath("$.data.role").value("USER"));
        }
    }

    @Nested
    @DisplayName("Update Current User Profile")
    class UpdateMe {

        @Test
        @WithMockUser(username = "john@doe.com", roles = {"USER"})
        void shouldUpdateCurrentUserProfile() throws Exception {
            var request = new UserDto.UserUpdateRequest();
            request.setName("Updated");
            request.setEmail("john@doe.com");

            var updated = getUser(1L, "Updated", "john@doe.com", User.Role.USER);
            Mockito.when(userService.update(anyLong(), any())).thenReturn(updated);

            mockMvc.perform(put("/api/users/me")
                            .with(user("john@doe.com").roles("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name", is("Updated")));
        }
    }

    @Nested
    @DisplayName("Change Password")
    class ChangePassword {

        @Test
        @WithMockUser(username = "john@doe.com", roles = {"USER"})
        void shouldChangePassword() throws Exception {
            var request = new UserDto.PasswordChangeRequest();
            request.setOldPassword("oldpass");
            request.setNewPassword("newpass");

            mockMvc.perform(put("/api/users/me/password")
                            .with(user("john@doe.com").roles("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Admin endpoints")
    class AdminEndpoints {

        @Test
        @WithMockUser(username = "admin@admin.com", roles = {"ADMIN"})
        void shouldReturnUserById() throws Exception {
            var user = getUser(1L, "Admin", "admin@admin.com", User.Role.ADMIN);
            Mockito.when(userService.findById(1L)).thenReturn(user);

            mockMvc.perform(get("/api/users/1")
                            .with(user("admin@admin.com").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.email").value("admin@admin.com"));
        }

        @Test
        @WithMockUser(username = "admin@admin.com", roles = {"ADMIN"})
        void shouldReturnAllUsersPaged() throws Exception {
            var users = List.of(getUser(1L, "Admin", "admin@admin.com", User.Role.ADMIN));
            Mockito.when(userService.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(users, PageRequest.of(0, 20), 1));

            mockMvc.perform(get("/api/users")
                            .with(user("admin@admin.com").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].email").value("admin@admin.com"));
        }

        @Test
        @WithMockUser(username = "admin@admin.com", roles = {"ADMIN"})
        void shouldCreateNewUser() throws Exception {
            var req = new UserDto.UserCreateRequest();
            req.setName("Ahmed");
            req.setEmail("ahmed@domain.com");
            req.setPassword("secret123");
            req.setRole(User.Role.USER);

            var created = getUser(2L, "Ahmed", "ahmed@domain.com", User.Role.USER);
            Mockito.when(userService.register(any())).thenReturn(created);

            mockMvc.perform(post("/api/users")
                            .with(user("admin@admin.com").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.name").value("Ahmed"));
        }

        @Test
        @WithMockUser(username = "admin@admin.com", roles = {"ADMIN"})
        void shouldUpdateUserById() throws Exception {
            var req = new UserDto.UserUpdateRequest();
            req.setName("UpdatedName");
            req.setEmail("user@domain.com");

            var updated = getUser(3L, "UpdatedName", "user@domain.com", User.Role.USER);
            Mockito.when(userService.update(eq(3L), any())).thenReturn(updated);

            mockMvc.perform(put("/api/users/3")
                            .with(user("admin@admin.com").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name", is("UpdatedName")));
        }

        @Test
        @WithMockUser(username = "admin@admin.com", roles = {"ADMIN"})
        void shouldDeleteUser() throws Exception {
            Mockito.doNothing().when(userService).delete(4L);

            mockMvc.perform(delete("/api/users/4")
                            .with(user("admin@admin.com").roles("ADMIN")))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(username = "admin@admin.com", roles = {"ADMIN"})
        void shouldChangeUserRole() throws Exception {
            var req = new UserDto.RoleChangeRequest();
            req.setRole(User.Role.ADMIN);

            var updated = getUser(5L, "UserFive", "five@domain.com", User.Role.ADMIN);
            Mockito.when(userService.changeRole(eq(5L), eq(User.Role.ADMIN))).thenReturn(updated);

            mockMvc.perform(put("/api/users/5/role")
                            .with(user("admin@admin.com").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.role", is("ADMIN")));
        }
    }
}
