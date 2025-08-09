package com.veggieshop.common;

import lombok.*;
import java.time.Instant;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiError {
    private int status;
    private String error;
    private String message;
    private String path;
    @Builder.Default
    private Instant timestamp = Instant.now();
    private Map<String, String> fieldErrors;
}
