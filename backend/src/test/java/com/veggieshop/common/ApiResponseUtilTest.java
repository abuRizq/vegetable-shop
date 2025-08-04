package com.veggieshop.common;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ApiResponseUtilTest {

    @Test
    void ok_shouldReturnSuccessResponse_withData() {
        String data = "hello";
        ResponseEntity<ApiResponse<String>> response = ApiResponseUtil.ok(data);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo("hello");
        assertThat(response.getBody().getError()).isNull();
    }

    @Test
    void ok_withPage_shouldReturnSuccessResponse_withMeta() {
        List<String> items = List.of("a", "b");
        Pageable pageable = PageRequest.of(1, 2, Sort.by(Sort.Order.desc("id")));
        Page<String> page = new PageImpl<>(items, pageable, 10);

        ResponseEntity<ApiResponse<List<String>>> response = ApiResponseUtil.ok(page);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).containsExactly("a", "b");
        assertThat(response.getBody().getMeta()).isNotNull();
        assertThat(response.getBody().getMeta().getPage()).isEqualTo(1);
        assertThat(response.getBody().getMeta().getSize()).isEqualTo(2);
        assertThat(response.getBody().getMeta().getTotalElements()).isEqualTo(10);
        assertThat(response.getBody().getMeta().getSort().get(0)).startsWith("id,");
    }

    @Test
    void ok_withDataAndMeta_shouldReturnSuccessResponse() {
        String data = "hi";
        Meta meta = Meta.builder()
                .page(0).size(5)
                .totalElements(11).totalPages(3)
                .first(true).last(false)
                .sort(List.of("name,ASC"))
                .build();

        ResponseEntity<ApiResponse<String>> response = ApiResponseUtil.ok(data, meta);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo("hi");
        assertThat(response.getBody().getMeta().getTotalElements()).isEqualTo(11);
    }

    @Test
    void created_shouldReturnCreatedResponse() {
        String data = "created";
        ResponseEntity<ApiResponse<String>> response = ApiResponseUtil.created(data);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo("created");
    }

    @Test
    void error_withStatusMessagePath_shouldReturnErrorResponse() {
        String path = "/api/test";
        ResponseEntity<ApiResponse<Object>> response = ApiResponseUtil.error(HttpStatus.BAD_REQUEST, "Invalid input", path);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError()).isNotNull();
        assertThat(response.getBody().getError().getMessage()).contains("Invalid input");
        assertThat(response.getBody().getError().getPath()).isEqualTo(path);
        assertThat(response.getBody().getError().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError().getTimestamp()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void error_withApiErrorAndStatus_shouldReturnCustomErrorResponse() {
        ApiError error = ApiError.builder()
                .status(403)
                .error("Forbidden")
                .message("Access denied")
                .path("/secure")
                .timestamp(Instant.now())
                .build();

        ResponseEntity<ApiResponse<Object>> response = ApiResponseUtil.error(error, HttpStatus.FORBIDDEN);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError().getMessage()).contains("Access denied");
    }

    @Test
    void noContent_shouldReturn204Response() {
        ResponseEntity<ApiResponse<Void>> response = ApiResponseUtil.noContent();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }
}
