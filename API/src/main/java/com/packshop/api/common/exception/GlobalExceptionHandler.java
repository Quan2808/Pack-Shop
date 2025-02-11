package com.packshop.api.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorDetails> buildErrorResponse(HttpStatus status, String message, String details) {
        ErrorDetails errorDetails = new ErrorDetails(status.value(), message, details);
        return new ResponseEntity<>(errorDetails, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred while processing your request");
    }

    @ExceptionHandler({ NoSuchElementException.class, ResourceNotFoundException.class })
    public ResponseEntity<ErrorDetails> handleNotFoundException(Exception ex, WebRequest request) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleIllegalArgumentException(IllegalArgumentException ex,
            WebRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Request",
                ex.getMessage());
    }

    @ExceptionHandler({ ConstraintViolationException.class,
            org.hibernate.exception.ConstraintViolationException.class })
    public ResponseEntity<ErrorDetails> handleConstraintViolationException(Exception ex) {
        String details = ex.getMessage();

        // Handle specific database constraints
        if (ex instanceof org.hibernate.exception.ConstraintViolationException) {
            String constraintName = ((org.hibernate.exception.ConstraintViolationException) ex)
                    .getConstraintName().toLowerCase();

            // Unique constraint violations
            if (constraintName.contains("unique")) {
                if (constraintName.contains("category")) {
                    details = "A category with this name already exists";
                } else if (constraintName.contains("product")) {
                    details = "A product with this SKU already exists";
                } else {
                    details = "A duplicate entry was detected";
                }
            }
            // Foreign key violations
            else if (constraintName.contains("foreign")) {
                details = "The referenced entity does not exist";
            }
        }

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Error",
                details);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError
                    ? ((FieldError) error).getField()
                    : ((ObjectError) error).getObjectName();

            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                formatValidationErrors(errors));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorDetails> handleDuplicateResourceException(DuplicateResourceException ex) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Duplicate Resource",
                ex.getMessage());
    }

    private String formatValidationErrors(Map<String, String> errors) {
        return errors.entrySet().stream()
                .map(entry -> String.format("%s: %s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("; "));
    }
}