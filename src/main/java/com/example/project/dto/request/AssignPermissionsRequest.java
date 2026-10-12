package com.example.project.dto.request;

import java.util.Set;

public record AssignPermissionsRequest(
        Set<Long> permissionIds
) {}
