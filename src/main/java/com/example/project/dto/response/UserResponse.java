package com.example.project.dto.response;

import com.example.project.entity.User;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        Boolean enabled,
        Set<RoleResponse> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse fromEntity(User u) {
        Set<RoleResponse> roles = u.getRoles() == null ? Set.of() :
                u.getRoles().stream()
                        .map(RoleResponse::fromEntity)
                        .collect(Collectors.toSet());

        return new UserResponse(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getFullName(),
                u.getEnabled(),
                roles,
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }
}

