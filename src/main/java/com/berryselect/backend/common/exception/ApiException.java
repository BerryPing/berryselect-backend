package com.berryselect.backend.common.exception;

// API 비즈니스 로직 예외
//Service 계층에서 발생하는 예외들을 처리
public class ApiException extends RuntimeException {

    private String code;

    public ApiException() {
        super();
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiException(Throwable cause) {
        super(cause);
    }

    // 에러 코드와 메시지를 함께 받는 생성자
    public ApiException(String code, String message) {
        super(message);
        this.code = code;
    }

    // 에러 코드와 메시지, 원인을 함께 받는 생성자
    public ApiException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}