package com.packshop.api.modules.shopping.payment.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.packshop.api.common.exceptions.ResourceNotFoundException;
import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.shopping.payment.dto.PaymentRequestDTO;
import com.packshop.api.modules.shopping.payment.dto.PaymentTransactionDTO;
import com.packshop.api.modules.shopping.payment.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

  private final PaymentService paymentService;

  /**
   * Creates a PayPal payment for an order and returns the payment transaction
   * details.
   * 
   * @param orderId The ID of the order to create payment for
   * @param request DTO containing returnUrl and cancelUrl
   * @return PaymentTransactionDTO with payment details
   */
  @PostMapping("/create/{orderId}")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<PaymentTransactionDTO> createPayment(
      @PathVariable Long orderId,
      @Valid @RequestBody PaymentRequestDTO request,
      @AuthenticationPrincipal User payerId) {
    log.info("Received request to create payment for order ID: {}", orderId);
    try {
      PaymentTransactionDTO paymentDTO = paymentService.createPayment(
          orderId,
          payerId.getId(),
          request.getReturnUrl(),
          request.getCancelUrl());
      log.info("Payment created successfully for order ID: {}", orderId);
      return ResponseEntity.status(HttpStatus.CREATED).body(paymentDTO);
    } catch (ResourceNotFoundException e) {
      log.error("Order not found for payment creation: {}", orderId);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    } catch (RuntimeException e) {
      log.error("Failed to create payment for order ID: {}", orderId, e);
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Payment processing failed", e);
    }
  }

  /**
   * Executes a PayPal payment after user approval.
   * 
   * @param paymentId PayPal payment ID
   * @param payerId   PayPal payer ID
   * @return PaymentTransactionDTO with updated payment details
   */
  @PostMapping("/execute")
  public ResponseEntity<PaymentTransactionDTO> executePayment(
      @RequestParam("paymentId") String paymentId,
      @AuthenticationPrincipal User user) {
    log.info("Received request to execute payment with ID: {}", paymentId);
    try {
      PaymentTransactionDTO paymentDTO = paymentService.executePayment(paymentId, Long.toString(user.getId()));
      log.info("Payment executed successfully for payment ID: {}", paymentId);
      return ResponseEntity.ok(paymentDTO);
    } catch (ResourceNotFoundException e) {
      log.error("Payment transaction not found: {}", paymentId);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    } catch (RuntimeException e) {
      log.error("Failed to execute payment with ID: {}", paymentId, e);
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Payment execution failed", e);
    }
  }

  /**
   * Global exception handler for validation errors
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
    return ResponseEntity.badRequest().body(errors);
  }
}