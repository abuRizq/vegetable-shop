package com.veggieshop.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class SessionDto {
    private Long id;
    private String deviceInfo;
    private Instant expiryDate;
    private boolean revoked;
}
