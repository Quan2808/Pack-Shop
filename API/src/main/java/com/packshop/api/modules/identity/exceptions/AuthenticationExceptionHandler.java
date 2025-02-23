package com.packshop.api.modules.identity.exceptions;

import com.packshop.api.common.exceptions.DuplicateResourceException;
import com.packshop.api.common.exceptions.ResourceNotFoundException;
import com.packshop.api.modules.identity.dto.AuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class AuthenticationExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<AuthResponse> handleAuthenticationException(AuthenticationException e) {
        log.debug("Handling AuthenticationException: {}", e.getMessage());
        AuthResponse response = new AuthResponse();
        response.setMessage("Invalid username or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<AuthResponse> handleBadCredentialsException(BadCredentialsException e) {
        log.debug("Handling BadCredentialsException: {}", e.getMessage());
        AuthResponse response = new AuthResponse();
        response.setMessage("Invalid username or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<AuthResponse> handleDuplicateResourceException(DuplicateResourceException e) {
        log.debug("Handling DuplicateResourceException: {}", e.getMessage());
        AuthResponse response = new AuthResponse();
        response.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<AuthResponse> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.debug("Handling ResourceNotFoundException: {}", e.getMessage());
        AuthResponse response = new AuthResponse();
        response.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.debug("Handling MethodArgumentNotValidException: {}", e.getMessage());
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse("Validation failed");
        AuthResponse response = new AuthResponse();
        response.setMessage(errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponse> handleGenericException(Exception e) {
        log.error("Handling unexpected exception: {}", e.getMessage(), e);
        AuthResponse response = new AuthResponse();
        response.setMessage("An unexpected error occurred: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}