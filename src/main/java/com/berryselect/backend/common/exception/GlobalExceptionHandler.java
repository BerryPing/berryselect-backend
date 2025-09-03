package com.berryselect.backend.common.exception;

import com.berryselect.backend.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

// 전역 예외 처리 핸들러
// 모든 Controller에서 발생하는 예외를 일관된 형태로 응답
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ApiException 처리 (비즈니스 로직 예외)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException e, HttpServletRequest request) {
        log.warn("ApiException occurred: {} | URI: {}", e.getMessage(), request.getRequestURI());

        if (e.getCode() != null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), e.getCode()));
        }

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
    }

    // Validation 예외 처리 (@Valid 실패)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {

        log.warn("Validation failed: {} | URI: {}", e.getMessage(), request.getRequestURI());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(errors, "입력값 검증에 실패했습니다.", "VALIDATION_FAILED"));
    }

    // BindException 처리 (파라미터 바인딩 실패)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBindException(
            BindException e, HttpServletRequest request) {

        log.warn("Bind exception: {} | URI: {}", e.getMessage(), request.getRequestURI());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(errors, "파라미터 바인딩에 실패했습니다.", "BIND_FAILED"));
    }

    // 타입 변환 예외 처리 (PathVariable, RequestParam 타입 불일치)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {

        log.warn("Type mismatch: parameter '{}' should be of type '{}' | URI: {}",
                e.getName(), e.getRequiredType().getSimpleName(), request.getRequestURI());

        String message = String.format("파라미터 '%s'의 값이 올바르지 않습니다.", e.getName());

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(message, "INVALID_PARAMETER"));
    }

    // IllegalArgumentException 처리 (잘못된 인자)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException e, HttpServletRequest request) {

        log.warn("Illegal argument: {} | URI: {}", e.getMessage(), request.getRequestURI());

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), "INVALID_ARGUMENT"));
    }

    // IllegalStateException 처리 (잘못된 상태)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(
            IllegalStateException e, HttpServletRequest request) {

        log.warn("Illegal state: {} | URI: {}", e.getMessage(), request.getRequestURI());

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), "INVALID_STATE"));
    }

    // 일반적인 RuntimeException 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException e, HttpServletRequest request) {

        log.error("Unexpected runtime exception: {} | URI: {}", e.getMessage(), request.getRequestURI(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 내부 오류가 발생했습니다.", "INTERNAL_ERROR"));
    }

    // 모든 예외의 최종 처리 (예상하지 못한 예외)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e, HttpServletRequest request) {

        log.error("Unexpected exception: {} | URI: {}", e.getMessage(), request.getRequestURI(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("알 수 없는 오류가 발생했습니다.", "UNKNOWN_ERROR"));
    }
}