package com.packshop.client.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class CatalogServerException extends RuntimeException {
    public CatalogServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
