package com.veggieshop.auth;

import org.mapstruct.Mapper;
import com.veggieshop.auth.SessionDto;
import com.veggieshop.auth.RefreshToken;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    SessionDto toSessionDto(RefreshToken refreshToken);
}
