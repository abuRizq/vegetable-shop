package com.veggieshop.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veggieshop.common.ApiResponse;
import com.veggieshop.common.Meta;
import com.veggieshop.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ProductService productService;

    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;


    @Autowired
    ObjectMapper objectMapper;

    // Helper for test data
    ProductDto.ProductResponse sampleProduct() {
        ProductDto.ProductResponse resp = new ProductDto.ProductResponse();
        resp.setId(1L);
        resp.setName("Carrot");
        resp.setDescription("Fresh carrot");
        resp.setPrice(BigDecimal.valueOf(2.5));
        resp.setDiscount(BigDecimal.valueOf(0.5));
        resp.setFeatured(true);
        resp.setSoldCount(10L);
        resp.setImageUrl("carrot.jpg");
        resp.setCategoryId(1L);
        resp.setCategoryName("Root");
        resp.setActive(true);
        return resp;
    }

    @Test
    @DisplayName("GET /api/products - success paged")
    void getAllProducts_success() throws Exception {
        ProductDto.ProductResponse product = sampleProduct();
        Page<ProductDto.ProductResponse> page = new PageImpl<>(
                List.of(product), PageRequest.of(0, 20), 1
        );
        Mockito.when(productService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(product.getId()))
                .andExpect(jsonPath("$.data[0].name").value(product.getName()))
                .andExpect(jsonPath("$.meta.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/products/{id} - found")
    void getProductById_success() throws Exception {
        ProductDto.ProductResponse product = sampleProduct();
        Mockito.when(productService.findById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("GET /api/products/{id} - not found")
    void getProductById_notFound() throws Exception {
        Mockito.when(productService.findById(1L)).thenThrow(new com.veggieshop.exception.ResourceNotFoundException("Product not found"));
        mockMvc.perform(get("/api/products/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Product not found"));
    }

    @Test
    @DisplayName("GET /api/products/featured - paged")
    void getFeaturedProducts_success() throws Exception {
        ProductDto.ProductResponse product = sampleProduct();
        Page<ProductDto.ProductResponse> page = new PageImpl<>(List.of(product));
        Mockito.when(productService.findFeatured(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/products/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].featured").value(true));
    }

    @Test
    @DisplayName("GET /api/products/category/{categoryId} - paged")
    void getProductsByCategory_success() throws Exception {
        ProductDto.ProductResponse product = sampleProduct();
        Page<ProductDto.ProductResponse> page = new PageImpl<>(List.of(product));
        Mockito.when(productService.findByCategory(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/products/category/{categoryId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].categoryId").value(1));
    }

    @Test
    @DisplayName("GET /api/products/search?name=carrot - paged")
    void searchProductsByName_success() throws Exception {
        ProductDto.ProductResponse product = sampleProduct();
        Page<ProductDto.ProductResponse> page = new PageImpl<>(List.of(product));
        Mockito.when(productService.searchByName(eq("carrot"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/products/search")
                        .param("name", "carrot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Carrot"));
    }

    @Test
    @DisplayName("GET /api/products/filter?min=1&max=5 - paged")
    void filterProductsByPrice_success() throws Exception {
        ProductDto.ProductResponse product = sampleProduct();
        Page<ProductDto.ProductResponse> page = new PageImpl<>(List.of(product));
        Mockito.when(productService.filterByPrice(any(BigDecimal.class), any(BigDecimal.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/products/filter")
                        .param("min", "1")
                        .param("max", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].price").value(2.5));
    }

    @Test
    @DisplayName("POST /api/products - create product (admin only)")
    @WithMockUser(roles = "ADMIN")
    void createProduct_success() throws Exception {
        ProductDto.ProductCreateRequest req = new ProductDto.ProductCreateRequest();
        req.setName("Apple");
        req.setDescription("Red Apple");
        req.setPrice(BigDecimal.valueOf(3));
        req.setDiscount(BigDecimal.ZERO);
        req.setFeatured(false);
        req.setCategoryId(2L);
        req.setImageUrl("apple.jpg");

        ProductDto.ProductResponse resp = sampleProduct();
        resp.setName("Apple");

        Mockito.when(productService.create(any(ProductDto.ProductCreateRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Apple"));
    }

    @Test
    @DisplayName("PUT /api/products/{id} - update product (admin only)")
    @WithMockUser(roles = "ADMIN")
    void updateProduct_success() throws Exception {
        ProductDto.ProductUpdateRequest req = new ProductDto.ProductUpdateRequest();
        req.setName("Updated Name");
        req.setDescription("Updated Desc");
        req.setPrice(BigDecimal.valueOf(5));
        req.setDiscount(BigDecimal.ONE);
        req.setFeatured(false);
        req.setCategoryId(2L);
        req.setImageUrl("updated.jpg");

        ProductDto.ProductResponse resp = sampleProduct();
        resp.setName("Updated Name");

        Mockito.when(productService.update(eq(1L), any(ProductDto.ProductUpdateRequest.class))).thenReturn(resp);

        mockMvc.perform(put("/api/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Name"));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - delete product (admin only)")
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_success() throws Exception {
        Mockito.doNothing().when(productService).delete(1L);

        mockMvc.perform(delete("/api/products/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
