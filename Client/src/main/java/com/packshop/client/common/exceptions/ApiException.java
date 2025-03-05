package com.packshop.client.common.exceptions;

import java.util.Map;

public class ApiException extends RuntimeException {
    private final int statusCode;
    private final String errorCode; // Thêm để lưu mã lỗi từ ErrorDetails
    private final Map<String, String> errors; // Thêm để lưu chi tiết lỗi

    public ApiException(String message, int statusCode, String errorCode, Map<String, String> errors) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.errors = errors;
    }

    public ApiException(String message, int statusCode, String errorCode, Map<String, String> errors, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.errors = errors;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500; // Sử dụng 500 thay vì -1 cho lỗi mặc định
        this.errorCode = "UNKNOWN_ERROR";
        this.errors = null;
    }

    public ApiException(String message) {
        super(message);
        this.statusCode = 500;
        this.errorCode = "UNKNOWN_ERROR";
        this.errors = null;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}