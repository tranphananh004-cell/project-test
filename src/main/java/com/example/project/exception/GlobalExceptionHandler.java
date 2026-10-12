package com.example.project.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * DTO đại diện cho response trả về khi xảy ra lỗi.
     */
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorResponse(
            int status,
            String error,
            String message,
            LocalDateTime timestamp,
            Map<String, String> fieldErrors     // validation errors
    ) {
        public static ErrorResponse of(ErrorCode errorCode) {
            return ErrorResponse.builder()
                    .status(errorCode.getHttpStatus().value())
                    .error(errorCode.name())
                    .message(errorCode.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        public static ErrorResponse of(ErrorCode errorCode, String message) {
            return ErrorResponse.builder()
                    .status(errorCode.getHttpStatus().value())
                    .error(errorCode.name())
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        public static ErrorResponse of(ErrorCode errorCode, Map<String, String> fieldErrors) {
            return ErrorResponse.builder()
                    .status(errorCode.getHttpStatus().value())
                    .error(errorCode.name())
                    .message(errorCode.getMessage())
                    .timestamp(LocalDateTime.now())
                    .fieldErrors(fieldErrors)
                    .build();
        }
    }

    /**
     * Xử lý lỗi nghiệp vụ từ các service (AppException).
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        log.warn("AppException: {} - {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    /**
     * Xử lý lỗi validation (@Valid, @NotBlank, v.v.).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (a, b) -> a   // nếu trùng field, giữ lỗi đầu tiên
                ));
        log.warn("Validation failed: {}", fieldErrors);
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(ErrorCode.VALIDATION_FAILED, fieldErrors));
    }

    /**
     * Xử lý lỗi JWT hết hạn.
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(ExpiredJwtException ex) {
        log.debug("JWT expired: {}", ex.getMessage());
        return ResponseEntity
                .status(ErrorCode.TOKEN_EXPIRED.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.TOKEN_EXPIRED));
    }

    /**
     * Xử lý lỗi JWT không hợp lệ.
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(JwtException ex) {
        log.warn("JWT invalid: {}", ex.getMessage());
        return ResponseEntity
                .status(ErrorCode.TOKEN_INVALID.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.TOKEN_INVALID));
    }

    /**
     * Xử lý lỗi sai credentials (Spring Security).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(ErrorCode.INVALID_CREDENTIALS.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_CREDENTIALS));
    }

    /**
     * Xử lý lỗi không có quyền.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(ErrorCode.ACCESS_DENIED.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.ACCESS_DENIED));
    }

    /**
     * Fallback — catch tất cả exception không lường trước.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR));
    }
}
