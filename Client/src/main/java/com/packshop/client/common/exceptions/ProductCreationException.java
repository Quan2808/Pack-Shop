package com.packshop.client.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProductCreationException extends RuntimeException {
    public ProductCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
