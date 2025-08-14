package com.veggieshop.unit.offer;

import com.veggieshop.exception.ResourceNotFoundException;
import com.veggieshop.offer.*;
import com.veggieshop.product.Product;
import com.veggieshop.product.ProductRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class OfferServiceTest {

    @Mock
    private OfferRepository offerRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OfferMapper offerMapper;

    @InjectMocks
    private OfferServiceImpl offerService;

    private AutoCloseable closeable;

    private final Product tomato = Product.builder().id(1L).name("Fresh Tomato").build();
    private final Product apple = Product.builder().id(2L).name("Golden Apple").build();

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // ========== Create ==========
    @Test
    void create_shouldSucceed_whenProductExists() {
        OfferDto.OfferCreateRequest req = new OfferDto.OfferCreateRequest();
        req.setProductId(1L);
        req.setDiscount(BigDecimal.valueOf(0.25));
        req.setStartDate(LocalDate.of(2024, 8, 1));
        req.setEndDate(LocalDate.of(2024, 8, 15));

        when(productRepository.findById(1L)).thenReturn(Optional.of(tomato));

        Offer offer = Offer.builder()
                .id(10L)
                .product(tomato)
                .discount(BigDecimal.valueOf(0.25))
                .startDate(LocalDate.of(2024, 8, 1))
                .endDate(LocalDate.of(2024, 8, 15))
                .build();

        when(offerRepository.save(any(Offer.class))).thenReturn(offer);

        OfferDto.OfferResponse resp = new OfferDto.OfferResponse();
        resp.setId(10L);
        resp.setProductId(1L);
        resp.setProductName("Fresh Tomato");
        resp.setDiscount(BigDecimal.valueOf(0.25));
        resp.setStartDate(LocalDate.of(2024, 8, 1));
        resp.setEndDate(LocalDate.of(2024, 8, 15));
        when(offerMapper.toOfferResponse(offer)).thenReturn(resp);

        OfferDto.OfferResponse result = offerService.create(req);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getDiscount()).isEqualTo(BigDecimal.valueOf(0.25));
        verify(offerRepository).save(any(Offer.class));
    }

    @Test
    void create_shouldThrow_whenProductMissing() {
        OfferDto.OfferCreateRequest req = new OfferDto.OfferCreateRequest();
        req.setProductId(99L);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> offerService.create(req));
        verify(offerRepository, never()).save(any());
    }

    // ========== Delete ==========
    @Test
    void delete_shouldSucceed_whenOfferExists() {
        when(offerRepository.existsById(5L)).thenReturn(true);
        offerService.delete(5L);
        verify(offerRepository).deleteById(5L);
    }

    @Test
    void delete_shouldThrow_whenOfferMissing() {
        when(offerRepository.existsById(77L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> offerService.delete(77L));
        verify(offerRepository, never()).deleteById(any());
    }

    // ========== Find By Id ==========
    @Test
    void findById_shouldReturnOffer_whenExists() {
        Offer offer = Offer.builder()
                .id(6L)
                .product(apple)
                .discount(BigDecimal.valueOf(1.0))
                .startDate(LocalDate.of(2024, 8, 5))
                .endDate(LocalDate.of(2024, 8, 12))
                .build();
        when(offerRepository.findById(6L)).thenReturn(Optional.of(offer));

        OfferDto.OfferResponse resp = new OfferDto.OfferResponse();
        resp.setId(6L);
        resp.setProductId(2L);
        resp.setProductName("Golden Apple");
        resp.setDiscount(BigDecimal.valueOf(1.0));
        resp.setStartDate(LocalDate.of(2024, 8, 5));
        resp.setEndDate(LocalDate.of(2024, 8, 12));
        when(offerMapper.toOfferResponse(offer)).thenReturn(resp);

        OfferDto.OfferResponse result = offerService.findById(6L);
        assertThat(result.getProductName()).isEqualTo("Golden Apple");
    }

    @Test
    void findById_shouldThrow_whenOfferMissing() {
        when(offerRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> offerService.findById(404L));
    }

    // ========== Find All ==========
    @Test
    void findAll_shouldReturnPagedOffers() {
        Pageable pageable = PageRequest.of(0, 2);
        Offer o1 = Offer.builder().id(1L).product(tomato).build();
        Offer o2 = Offer.builder().id(2L).product(apple).build();
        Page<Offer> page = new PageImpl<>(List.of(o1, o2), pageable, 2);

        when(offerRepository.findAll(pageable)).thenReturn(page);
        when(offerMapper.toOfferResponse(o1)).thenReturn(new OfferDto.OfferResponse());
        when(offerMapper.toOfferResponse(o2)).thenReturn(new OfferDto.OfferResponse());

        Page<OfferDto.OfferResponse> result = offerService.findAll(pageable);
        assertThat(result.getContent()).hasSize(2);
    }

    // ========== Find By Product ==========
    @Test
    void findByProduct_shouldReturnPagedOffers() {
        Pageable pageable = PageRequest.of(0, 1);
        Offer o1 = Offer.builder().id(3L).product(apple).build();
        Page<Offer> page = new PageImpl<>(List.of(o1), pageable, 1);

        when(offerRepository.findByProductId(2L, pageable)).thenReturn(page);
        when(offerMapper.toOfferResponse(o1)).thenReturn(new OfferDto.OfferResponse());

        Page<OfferDto.OfferResponse> result = offerService.findByProduct(2L, pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    // ========== Find Active Offers ==========
    @Test
    void findActiveOffers_shouldReturnPagedActiveOffers() {
        Pageable pageable = PageRequest.of(0, 2);
        LocalDate today = LocalDate.now();
        Offer o1 = Offer.builder().id(1L).product(tomato)
                .startDate(today.minusDays(1)).endDate(today.plusDays(2)).build();
        Offer o2 = Offer.builder().id(2L).product(apple)
                .startDate(today.minusDays(3)).endDate(today.plusDays(1)).build();

        Page<Offer> page = new PageImpl<>(List.of(o1, o2), pageable, 2);
        when(offerRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                today, today, pageable)).thenReturn(page);

        when(offerMapper.toOfferResponse(o1)).thenReturn(new OfferDto.OfferResponse());
        when(offerMapper.toOfferResponse(o2)).thenReturn(new OfferDto.OfferResponse());

        Page<OfferDto.OfferResponse> result = offerService.findActiveOffers(pageable);

        assertThat(result.getContent()).hasSize(2);
    }
}
