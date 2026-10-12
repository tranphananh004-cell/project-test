package com.example.project.dto.response;

import lombok.Builder;

import java.util.Set;

@Builder
public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,             // seconds
        Long userId,
        String username,
        String email,
        Set<String> permissions
) {}

