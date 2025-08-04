package com.veggieshop.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veggieshop.common.ApiResponse;
import com.veggieshop.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;



    @Autowired
    private ObjectMapper objectMapper;

    private CategoryDto.CategoryCreateRequest createRequest;
    private CategoryDto.CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        createRequest = new CategoryDto.CategoryCreateRequest();
        createRequest.setName("Fruits");
        createRequest.setDescription("Fresh fruits");

        categoryResponse = new CategoryDto.CategoryResponse();
        categoryResponse.setId(1L);
        categoryResponse.setName("Fruits");
        categoryResponse.setDescription("Fresh fruits");
    }

    @Test
    void shouldCreateCategory() throws Exception {
        when(categoryService.create(any(CategoryDto.CategoryCreateRequest.class)))
                .thenReturn(categoryResponse);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Fruits"))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    void shouldUpdateCategory() throws Exception {
        Long id = 1L;
        CategoryDto.CategoryUpdateRequest updateRequest = new CategoryDto.CategoryUpdateRequest();
        updateRequest.setName("Herbs");
        updateRequest.setDescription("Aromatic herbs");

        CategoryDto.CategoryResponse updatedResponse = new CategoryDto.CategoryResponse();
        updatedResponse.setId(id);
        updatedResponse.setName("Herbs");
        updatedResponse.setDescription("Aromatic herbs");

        when(categoryService.update(eq(id), any(CategoryDto.CategoryUpdateRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Herbs"));
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        doNothing().when(categoryService).delete(1L);

        mockMvc.perform(delete("/api/categories/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetCategoryById() throws Exception {
        when(categoryService.findById(1L)).thenReturn(categoryResponse);

        mockMvc.perform(get("/api/categories/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Fruits"));
    }

    @Test
    void shouldGetAllCategoriesPaged() throws Exception {
        Page<CategoryDto.CategoryResponse> page = new PageImpl<>(List.of(categoryResponse));
        when(categoryService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/categories")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Fruits"));
    }

    @Test
    void shouldSearchCategoriesByName() throws Exception {
        Page<CategoryDto.CategoryResponse> page = new PageImpl<>(List.of(categoryResponse));
        when(categoryService.searchByName(eq("Fru"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/categories/search")
                        .param("name", "Fru")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Fruits"));
    }
}
