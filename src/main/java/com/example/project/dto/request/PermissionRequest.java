package com.example.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PermissionRequest(
        @NotBlank(message = "Tên permission không được để trống")
        @Size(max = 100, message = "Tên permission tối đa 100 ký tự")
        String name,

        @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
        String description,

        @NotBlank(message = "Tài nguyên không được để trống")
        String resource,

        @NotBlank(message = "Hành động không được để trống")
        String action
) {}
