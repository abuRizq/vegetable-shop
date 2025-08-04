package com.veggieshop.offer;

import com.veggieshop.exception.ResourceNotFoundException;
import com.veggieshop.product.Product;
import com.veggieshop.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OfferServiceImplTest {

    @Mock
    private OfferRepository offerRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OfferMapper offerMapper;

    @InjectMocks
    private OfferServiceImpl offerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ===== CREATE =====
    @Test
    void shouldCreateOffer_whenProductExists() {
        OfferDto.OfferCreateRequest request = new OfferDto.OfferCreateRequest();
        request.setProductId(10L);
        request.setDiscount(new BigDecimal("0.3"));
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));

        Product product = Product.builder().id(10L).name("Apple").build();
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        Offer offer = Offer.builder()
                .id(1L)
                .product(product)
                .discount(new BigDecimal("0.3"))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        when(offerRepository.save(any(Offer.class))).thenReturn(offer);

        OfferDto.OfferResponse resp = new OfferDto.OfferResponse();
        resp.setId(1L);
        resp.setProductId(10L);

        when(offerMapper.toOfferResponse(offer)).thenReturn(resp);

        OfferDto.OfferResponse created = offerService.create(request);

        assertThat(created).isNotNull();
        assertThat(created.getProductId()).isEqualTo(10L);
        verify(offerRepository).save(any(Offer.class));
    }

    @Test
    void shouldThrowResourceNotFound_whenProductDoesNotExist() {
        OfferDto.OfferCreateRequest request = new OfferDto.OfferCreateRequest();
        request.setProductId(99L);

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offerService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    // ===== DELETE =====
    @Test
    void shouldDeleteOffer_whenExists() {
        when(offerRepository.existsById(3L)).thenReturn(true);

        offerService.delete(3L);

        verify(offerRepository).deleteById(3L);
    }

    @Test
    void shouldThrowResourceNotFound_whenDeleteNotFound() {
        when(offerRepository.existsById(77L)).thenReturn(false);

        assertThatThrownBy(() -> offerService.delete(77L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Offer not found");
    }

    // ===== FIND BY ID =====
    @Test
    void shouldFindOfferById_whenExists() {
        Offer offer = Offer.builder().id(8L).build();
        when(offerRepository.findById(8L)).thenReturn(Optional.of(offer));
        OfferDto.OfferResponse resp = new OfferDto.OfferResponse();
        resp.setId(8L);

        when(offerMapper.toOfferResponse(offer)).thenReturn(resp);

        OfferDto.OfferResponse found = offerService.findById(8L);

        assertThat(found).isNotNull().extracting(OfferDto.OfferResponse::getId).isEqualTo(8L);
    }

    @Test
    void shouldThrowResourceNotFound_whenFindByIdNotFound() {
        when(offerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offerService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Offer not found");
    }

    // ===== FIND ALL (Paged) =====
    @Test
    void shouldReturnAllOffersPaged() {
        Offer o1 = Offer.builder().id(1L).build();
        Offer o2 = Offer.builder().id(2L).build();
        Page<Offer> page = new PageImpl<>(List.of(o1, o2));
        Pageable pageable = PageRequest.of(0, 5);

        when(offerRepository.findAll(pageable)).thenReturn(page);

        OfferDto.OfferResponse r1 = new OfferDto.OfferResponse();
        r1.setId(1L);
        OfferDto.OfferResponse r2 = new OfferDto.OfferResponse();
        r2.setId(2L);

        when(offerMapper.toOfferResponse(o1)).thenReturn(r1);
        when(offerMapper.toOfferResponse(o2)).thenReturn(r2);

        Page<OfferDto.OfferResponse> result = offerService.findAll(pageable);

        assertThat(result.getContent()).hasSize(2);
    }

    // ===== FIND BY PRODUCT (Paged) =====
    @Test
    void shouldFindOffersByProductIdPaged() {
        Offer offer = Offer.builder().id(99L).build();
        Page<Offer> page = new PageImpl<>(List.of(offer));
        Pageable pageable = PageRequest.of(0, 2);

        when(offerRepository.findByProductId(33L, pageable)).thenReturn(page);

        OfferDto.OfferResponse resp = new OfferDto.OfferResponse();
        resp.setId(99L);

        when(offerMapper.toOfferResponse(offer)).thenReturn(resp);

        Page<OfferDto.OfferResponse> result = offerService.findByProduct(33L, pageable);

        assertThat(result.getContent()).hasSize(1)
                .extracting(OfferDto.OfferResponse::getId).containsExactly(99L);
    }

    // ===== FIND ACTIVE OFFERS (Paged) =====
    @Test
    void shouldFindActiveOffersPaged() {
        Offer offer = Offer.builder().id(5L).build();
        Page<Offer> page = new PageImpl<>(List.of(offer));
        Pageable pageable = PageRequest.of(0, 3);

        LocalDate today = LocalDate.now();
        when(offerRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today, pageable))
                .thenReturn(page);

        OfferDto.OfferResponse resp = new OfferDto.OfferResponse();
        resp.setId(5L);
        when(offerMapper.toOfferResponse(offer)).thenReturn(resp);

        Page<OfferDto.OfferResponse> result = offerService.findActiveOffers(pageable);

        assertThat(result.getContent()).hasSize(1)
                .extracting(OfferDto.OfferResponse::getId).containsExactly(5L);
    }
}
