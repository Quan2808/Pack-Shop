package com.packshop.client.common.exceptions;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorDetails {
    private int status;
    private String message;
    private String errorCode;
    private long timestamp;
    private String path;
    private Map<String, String> errors;

    public ErrorDetails(int status, String message, String errorCode, String path, Map<String, String> errors) {
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
        this.path = path;
        this.errors = errors;
    }
}