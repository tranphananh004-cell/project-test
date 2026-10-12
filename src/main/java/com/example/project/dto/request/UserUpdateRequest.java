package com.example.project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserUpdateRequest(
        @Email(message = "Email không đúng định dạng")
        String email,

        String fullName,

        @Size(min = 6, message = "Password mới phải có ít nhất 6 ký tự")
        String newPassword,

        Set<Long> roleIds,

        Boolean enabled
) {}

