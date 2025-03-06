package com.packshop.client.common.exceptions;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetails {
    @JsonProperty("status")
    private int status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("path")
    private String path;

    @JsonProperty("errors")
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