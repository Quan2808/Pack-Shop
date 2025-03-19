package com.packshop.api.modules.shopping.payment;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class PaymentTransactionDTO {
  private Long id;
  private String transactionId;
  private String requestId;
  private String partnerCode;
  private Long amount;
  private PaymentTransaction.PaymentStatus status;
  private String payUrl;
  private String qrCodeUrl;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'HH:mm")
  private LocalDateTime transactionDate;
  private String responseMessage;
}
