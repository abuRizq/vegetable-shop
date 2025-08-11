package com.veggieshop.unit.common;

import com.veggieshop.common.dto.ApiError;
import com.veggieshop.common.ApiResponse;
import com.veggieshop.common.ApiResponseUtil;
import com.veggieshop.common.Meta;
import org.junit.jupiter.api.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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
    void ok_shouldReturnSuccessResponse_withDataAndMeta() {
        String data = "test";
        Meta meta = Meta.builder()
                .page(1).size(10).totalElements(100L).totalPages(10)
                .first(true).last(false).sort(List.of("id,ASC")).build();
        ResponseEntity<ApiResponse<String>> response = ApiResponseUtil.ok(data, meta);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo("test");
        assertThat(response.getBody().getMeta()).isEqualTo(meta);
        assertThat(response.getBody().getError()).isNull();
    }

    @Test
    void ok_shouldReturnSuccessResponse_withPage() {
        List<String> content = List.of("a", "b", "c");
        Page<String> page = new PageImpl<>(
                content,
                PageRequest.of(0, 3, Sort.by(Sort.Order.asc("name"))),
                9
        );

        ResponseEntity<ApiResponse<List<String>>> response = ApiResponseUtil.ok(page);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo(content);
        assertThat(response.getBody().getMeta()).isNotNull();
        Meta meta = response.getBody().getMeta();
        assertThat(meta.getPage()).isEqualTo(0);
        assertThat(meta.getSize()).isEqualTo(3);
        assertThat(meta.getTotalElements()).isEqualTo(9);
        assertThat(meta.getSort()).containsExactly("name,ASC");
    }

    @Test
    void created_shouldReturnCreatedResponse() {
        String data = "created";
        ResponseEntity<ApiResponse<String>> response = ApiResponseUtil.created(data);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo("created");
        assertThat(response.getBody().getError()).isNull();
    }

    @Test
    void error_shouldReturnErrorResponse_withStatus_andMessage_andPath() {
        String message = "Not Found";
        String path = "/api/test";
        ResponseEntity<ApiResponse<Object>> response = ApiResponseUtil.error(HttpStatus.NOT_FOUND, message, path);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getData()).isNull();
        ApiError err = response.getBody().getError();
        assertThat(err).isNotNull();
        assertThat(err.getStatus()).isEqualTo(404);
        assertThat(err.getError()).isEqualTo("Not Found");
        assertThat(err.getMessage()).isEqualTo(message);
        assertThat(err.getPath()).isEqualTo(path);
        assertThat(err.getTimestamp()).isNotNull();
    }

    @Test
    void error_shouldReturnErrorResponse_withApiError_andStatus() {
        ApiError apiError = ApiError.builder()
                .status(409)
                .error("Conflict")
                .message("Duplicate entry")
                .path("/api/conflict")
                .timestamp(Instant.now())
                .fieldErrors(Map.of("email", "already exists"))
                .build();

        ResponseEntity<ApiResponse<Object>> response = ApiResponseUtil.error(apiError, HttpStatus.CONFLICT);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getData()).isNull();
        assertThat(response.getBody().getError()).isEqualTo(apiError);
    }

    @Test
    void noContent_shouldReturnNoContentResponse() {
        ResponseEntity<ApiResponse<Object>> response = ApiResponseUtil.noContent();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isNull();
        assertThat(response.getBody().getMeta()).isNull();
        assertThat(response.getBody().getError()).isNull();
    }
}
