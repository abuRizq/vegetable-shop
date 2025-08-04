package com.veggieshop.offer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veggieshop.common.ApiResponse;
import com.veggieshop.exception.ResourceNotFoundException;
import com.veggieshop.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
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
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OfferController.class)
@AutoConfigureMockMvc(addFilters = false)
class OfferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OfferService offerService;

    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;



    @Autowired
    private ObjectMapper objectMapper;

    private OfferDto.OfferResponse offerResponse;
    private OfferDto.OfferCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        offerResponse = new OfferDto.OfferResponse();
        offerResponse.setId(1L);
        offerResponse.setProductId(10L);
        offerResponse.setDiscount(new BigDecimal("0.2"));
        offerResponse.setStartDate(LocalDate.now());
        offerResponse.setEndDate(LocalDate.now().plusDays(5));

        createRequest = new OfferDto.OfferCreateRequest();
        createRequest.setProductId(10L);
        createRequest.setDiscount(new BigDecimal("0.2"));
        createRequest.setStartDate(LocalDate.now());
        createRequest.setEndDate(LocalDate.now().plusDays(5));
    }

    @Test
    void getAllPaged_shouldReturnOffers() throws Exception {
        Page<OfferDto.OfferResponse> page = new PageImpl<>(List.of(offerResponse));
        when(offerService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/offers")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void getById_shouldReturnOffer() throws Exception {
        when(offerService.findById(1L)).thenReturn(offerResponse);

        mockMvc.perform(get("/api/offers/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(offerService.findById(999L)).thenThrow(new ResourceNotFoundException("Offer not found"));

        mockMvc.perform(get("/api/offers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByProductPaged_shouldReturnOffersForProduct() throws Exception {
        Page<OfferDto.OfferResponse> page = new PageImpl<>(List.of(offerResponse));
        when(offerService.findByProduct(eq(10L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/offers/product/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].productId").value(10));
    }

    @Test
    void getActiveOffers_shouldReturnActiveOffers() throws Exception {
        Page<OfferDto.OfferResponse> page = new PageImpl<>(List.of(offerResponse));
        when(offerService.findActiveOffers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/offers/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturnCreatedOffer_whenAdmin() throws Exception {
        when(offerService.create(any(OfferDto.OfferCreateRequest.class))).thenReturn(offerResponse);

        mockMvc.perform(post("/api/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void create_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(post("/api/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_shouldReturn204_whenAdmin() throws Exception {
        doNothing().when(offerService).delete(1L);

        mockMvc.perform(delete("/api/offers/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/offers/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_shouldReturn404_whenOfferNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Offer not found")).when(offerService).delete(999L);

        mockMvc.perform(delete("/api/offers/999"))
                .andExpect(status().isNotFound());
    }
}
