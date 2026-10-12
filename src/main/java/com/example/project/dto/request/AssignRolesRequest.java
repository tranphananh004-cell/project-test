package com.example.project.dto.request;

import java.util.Set;

public record AssignRolesRequest(
        Set<Long> roleIds
) {}
