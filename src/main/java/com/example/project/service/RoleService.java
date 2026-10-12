package com.example.project.service;

import com.example.project.dto.request.AssignPermissionsRequest;
import com.example.project.dto.request.RoleRequest;
import com.example.project.dto.response.RoleResponse;
import com.example.project.entity.Permission;
import com.example.project.entity.Role;
import com.example.project.exception.AppException;
import com.example.project.exception.ErrorCode;
import com.example.project.repository.PermissionRepository;
import com.example.project.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(RoleResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        return RoleResponse.fromEntity(role);
    }

    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.existsByName(request.name())) {
            throw new AppException(ErrorCode.ROLE_NAME_ALREADY_EXISTS);
        }

        Role role = Role.builder()
                .name(request.name())
                .description(request.description())
                .isSystem(false)
                .build();

        if (request.permissionIds() != null && !request.permissionIds().isEmpty()) {
            Set<Permission> permissions = permissionRepository.findByIdIn(request.permissionIds());
            role.setPermissions(permissions);
        }

        Role saved = roleRepository.save(role);
        log.info("Created role: {} (id: {})", saved.getName(), saved.getId());
        return RoleResponse.fromEntity(saved);
    }

    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        // Không cho phép đổi tên Role hệ thống
        if (Boolean.TRUE.equals(role.getIsSystem()) && !role.getName().equalsIgnoreCase(request.name())) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_ADMIN_ROLE);
        }

        if (!role.getName().equalsIgnoreCase(request.name()) && roleRepository.existsByName(request.name())) {
            throw new AppException(ErrorCode.ROLE_NAME_ALREADY_EXISTS);
        }

        role.setName(request.name());
        role.setDescription(request.description());

        if (request.permissionIds() != null) {
            Set<Permission> permissions = permissionRepository.findByIdIn(request.permissionIds());
            role.setPermissions(permissions);
        }

        Role updated = roleRepository.save(role);
        log.info("Updated role: {} (id: {})", updated.getName(), updated.getId());
        return RoleResponse.fromEntity(updated);
    }

    @Transactional
    public RoleResponse assignPermissionsToRole(Long id, AssignPermissionsRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        Set<Permission> permissions = new HashSet<>();
        if (request.permissionIds() != null && !request.permissionIds().isEmpty()) {
            permissions = permissionRepository.findByIdIn(request.permissionIds());
        }

        role.setPermissions(permissions);
        Role updated = roleRepository.save(role);
        log.info("Assigned {} permissions to role: {}", permissions.size(), updated.getName());
        return RoleResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        // Không cho phép xoá Role hệ thống
        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_ADMIN_ROLE);
        }

        if (!role.getUsers().isEmpty()) {
            throw new AppException(ErrorCode.ROLE_IN_USE);
        }

        role.getPermissions().clear();
        roleRepository.delete(role);
        log.info("Deleted role: {} (id: {})", role.getName(), id);
    }
}
