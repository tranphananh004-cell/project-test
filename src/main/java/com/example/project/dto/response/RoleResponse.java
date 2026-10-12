package com.example.project.dto.response;

import com.example.project.entity.Role;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record RoleResponse(
        Long id,
        String name,
        String description,
        Boolean isSystem,
        Set<PermissionResponse> permissions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RoleResponse fromEntity(Role r) {
        Set<PermissionResponse> perms = r.getPermissions() == null ? Set.of() :
                r.getPermissions().stream()
                        .map(PermissionResponse::fromEntity)
                        .collect(Collectors.toSet());

        return new RoleResponse(
                r.getId(),
                r.getName(),
                r.getDescription(),
                r.getIsSystem(),
                perms,
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}

