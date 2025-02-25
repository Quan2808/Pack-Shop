package com.packshop.client.common.exceptions;

public class ApiException extends RuntimeException {
    private final int statusCode;

    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1; // Mặc định khi không có status code
    }

    public ApiException(String message) {
        super(message);
        this.statusCode = -1;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
