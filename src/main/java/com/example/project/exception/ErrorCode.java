package com.example.project.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Auth errors
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Sai username hoặc password"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Token không hợp lệ"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token đã hết hạn"),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ hoặc đã bị thu hồi"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này"),

    // User errors
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy user"),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Username đã tồn tại"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email đã tồn tại"),
    CANNOT_DELETE_ADMIN_USER(HttpStatus.BAD_REQUEST, "Không được phép xoá tài khoản Admin hệ thống"),

    // Role errors
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy role"),
    ROLE_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Tên role đã tồn tại"),
    ROLE_IN_USE(HttpStatus.CONFLICT, "Role đang được gán cho người dùng, không thể xoá"),
    CANNOT_MODIFY_ADMIN_ROLE(HttpStatus.BAD_REQUEST, "Không được phép thay đổi hoặc xoá Role mặc định của hệ thống"),

    // Permission errors
    PERMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy permission"),
    PERMISSION_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Tên permission đã tồn tại"),

    // Generic
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống, vui lòng thử lại");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
