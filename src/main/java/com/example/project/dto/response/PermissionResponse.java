package com.example.project.dto.response;

import com.example.project.entity.Permission;

import java.time.LocalDateTime;

public record PermissionResponse(
        Long id,
        String name,
        String description,
        String resource,
        String action,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PermissionResponse fromEntity(Permission p) {
        return new PermissionResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getResource(),
                p.getAction(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}

