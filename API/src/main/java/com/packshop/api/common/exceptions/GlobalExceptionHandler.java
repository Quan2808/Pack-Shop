package com.packshop.api.common.exceptions;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String VALIDATION_FAILED_PREFIX = "Validation failed: ";
    private static final String INTERNAL_SERVER_ERROR_MSG = "An unexpected error occurred";

    @Autowired
    private HttpServletRequest request;

    // Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDetails> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        String errorMessage = VALIDATION_FAILED_PREFIX + errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("; "));

        log.debug("Validation failed for {} {}: {}", request.getMethod(), request.getRequestURI(), errorMessage);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorMessage, "VALIDATION_ERROR", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDetails> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.debug("Invalid request body for {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request body", "INVALID_REQUEST", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(AccessDeniedException ex) {
        log.debug("Access denied for {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Access denied", "FORBIDDEN", null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.debug("Resource not found for {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "RESOURCE_NOT_FOUND", null);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDetails> handleDuplicateResourceException(DuplicateResourceException ex) {
        log.debug("Duplicate resource found for {} {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "DUPLICATE_RESOURCE", null);
    }

    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDetails> handleInsufficientStockException(InsufficientStockException ex) {
        log.debug("Insufficient stock for {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "INSUFFICIENT_STOCK", null);
    }

    @ExceptionHandler(InvalidStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDetails> handleInvalidStatusException(InvalidStatusException ex) {
        log.debug("Insufficient stock for {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "INVALID_STATUS", null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDetails> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.debug("Invalid argument for {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "INVALID_ARGUMENT", null);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorDetails> handleAllExceptions(Exception ex) {
        log.error("Unexpected error occurred for {} {}: ", request.getMethod(), request.getRequestURI(), ex);
        String message = ex.getMessage() != null
                ? INTERNAL_SERVER_ERROR_MSG + ": " + ex.getMessage()
                : INTERNAL_SERVER_ERROR_MSG;
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, "INTERNAL_ERROR", null);
    }

    private ResponseEntity<ErrorDetails> buildErrorResponse(HttpStatus status, String message, String errorCode,
            Map<String, String> errors) {
        String path = (request != null) ? request.getRequestURI() : "unknown";
        return ResponseEntity
                .status(status)
                .body(new ErrorDetails(status.value(), message, errorCode, path, errors));
    }
}