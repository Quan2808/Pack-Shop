package com.packshop.client.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CatalogClientException extends RuntimeException {
    public CatalogClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
