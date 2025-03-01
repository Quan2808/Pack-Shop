package com.packshop.api.common.exceptions;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorDetails {
    private final int status;
    private final String message;
    private final Map<String, String> errors;
}