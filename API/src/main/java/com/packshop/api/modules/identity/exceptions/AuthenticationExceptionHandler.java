package com.packshop.api.modules.identity.exceptions;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.packshop.api.common.exceptions.DuplicateResourceException;
import com.packshop.api.common.exceptions.ResourceNotFoundException;
import com.packshop.api.modules.identity.dto.AuthResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class AuthenticationExceptionHandler {

    private static final String INVALID_CREDENTIALS_MSG = "Invalid username or password";
    private static final String VALIDATION_FAILED_PREFIX = "Validation failed: ";

    @ExceptionHandler({ AuthenticationException.class, BadCredentialsException.class })
    public ResponseEntity<AuthResponse> handleAuthenticationExceptions(Exception e) {
        log.debug("Authentication failed: {}", e.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS_MSG);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<AuthResponse> handleDuplicateResourceException(DuplicateResourceException e) {
        log.debug("Duplicate resource found: {}", e.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<AuthResponse> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.debug("Resource not found: {}", e.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthResponse> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = VALIDATION_FAILED_PREFIX + e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining("; "));

        log.debug("Validation failed: {}", errorMessage);
        return buildResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponse> handleGenericException(Exception e) {
        log.error("Unexpected error occurred: ", e);
        String message = "An unexpected error occurred" +
                (e.getMessage() != null ? ": " + e.getMessage() : "");
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    // Helper method to build responses
    private ResponseEntity<AuthResponse> buildResponse(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(AuthResponse.builder()
                        .message(message)
                        .build());
    }
}