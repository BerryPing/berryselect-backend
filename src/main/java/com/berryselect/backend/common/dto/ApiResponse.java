package com.berryselect.backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API 공통 응답 형태
 * @param <T> 응답 데이터 타입
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    // 성공 여부
    private boolean success;

    // 응답 메시지
    private String message;

    // 응답 데이터
    private T data;

    // 응답 코드 (선택)
    private String code;

    // 성공 응답 (데이터 있음)
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("성공")
                .data(data)
                .build();
    }

    // 성공 응답 (데이터 있음, 커스텀 메시지)
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    // 성공 응답 (데이터 없음)
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .success(true)
                .message("성공")
                .build();
    }

    // 성공 응답 (데이터 없음, 커스텀 메시지)
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    // 실패 응답 (메시지만)
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    // 실패 응답 (메시지 + 에러 코드)
    public static <T> ApiResponse<T> error(String message, String code) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .code(code)
                .build();
    }

    // 실패 응답 (데이터 + 메시지)
    public static <T> ApiResponse<T> error(T data, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .build();
    }

    // 실패 응답 (데이터 + 메시지 + 에러 코드)
    public static <T> ApiResponse<T> error(T data, String message, String code) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .code(code)
                .build();
    }
}