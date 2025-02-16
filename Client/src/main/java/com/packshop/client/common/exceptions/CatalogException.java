package com.packshop.client.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CatalogException extends RuntimeException {
    public CatalogException(String message, Throwable cause) {
        super(message, cause);
    }
}
