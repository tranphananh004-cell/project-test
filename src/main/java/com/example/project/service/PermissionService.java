package com.example.project.service;

import com.example.project.dto.request.PermissionRequest;
import com.example.project.dto.response.PermissionResponse;
import com.example.project.entity.Permission;
import com.example.project.entity.Role;
import com.example.project.exception.AppException;
import com.example.project.exception.ErrorCode;
import com.example.project.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions(String resource) {
        List<Permission> permissions;
        if (resource != null && !resource.isBlank()) {
            permissions = permissionRepository.findByResource(resource.trim());
        } else {
            permissions = permissionRepository.findAll();
        }
        return permissions.stream().map(PermissionResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public PermissionResponse getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        return PermissionResponse.fromEntity(permission);
    }

    @Transactional
    public PermissionResponse createPermission(PermissionRequest request) {
        if (permissionRepository.existsByName(request.name())) {
            throw new AppException(ErrorCode.PERMISSION_NAME_ALREADY_EXISTS);
        }

        Permission permission = Permission.builder()
                .name(request.name())
                .description(request.description())
                .resource(request.resource())
                .action(request.action())
                .build();

        Permission saved = permissionRepository.save(permission);
        log.info("Created permission: {} (id: {})", saved.getName(), saved.getId());
        return PermissionResponse.fromEntity(saved);
    }

    @Transactional
    public PermissionResponse updatePermission(Long id, PermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));

        if (!permission.getName().equalsIgnoreCase(request.name()) &&
                permissionRepository.existsByName(request.name())) {
            throw new AppException(ErrorCode.PERMISSION_NAME_ALREADY_EXISTS);
        }

        permission.setName(request.name());
        permission.setDescription(request.description());
        permission.setResource(request.resource());
        permission.setAction(request.action());

        Permission updated = permissionRepository.save(permission);
        log.info("Updated permission: {} (id: {})", updated.getName(), updated.getId());
        return PermissionResponse.fromEntity(updated);
    }

    @Transactional
    public void deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));

        // Bỏ liên kết với các role trước khi xoá để tránh lỗi khoá ngoại
        for (Role role : permission.getRoles()) {
            role.getPermissions().remove(permission);
        }

        permissionRepository.delete(permission);
        log.info("Deleted permission: {} (id: {})", permission.getName(), id);
    }
}
