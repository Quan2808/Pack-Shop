package com.packshop.api.modules.shopping.payment.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionDTO {
  private Long id;
  private Long orderId;
  private String paymentId;
  private String payerId;
  private PaymentStatus status;
  private LocalDateTime transactionDate;
  private Long amount;
  private String currency;

  public enum PaymentStatus {
    CREATED,
    APPROVED,
    COMPLETED,
    FAILED,
    CANCELLED
  }
}
