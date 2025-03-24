package com.packshop.api.modules.shopping.payment.entity;

import java.time.LocalDateTime;

import com.packshop.api.modules.shopping.order.entities.Order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Column(nullable = false)
  private String paymentId;

  @Column(nullable = false)
  private String payerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status;

  @Column(nullable = false)
  private LocalDateTime transactionDate;

  @Column(nullable = false)
  private Long amount;

  @Column(nullable = false)
  private String currency = "USD";

  public enum PaymentStatus {
    CREATED,
    APPROVED,
    COMPLETED,
    FAILED,
    CANCELLED
  }

  // public PaymentTransactionDTO toDTO() {
  // return new PaymentTransactionDTO(
  // this.id,
  // this.order.getId(),
  // this.paymentId,
  // this.payerId,
  // this.status,
  // this.transactionDate,
  // this.amount,
  // this.currency);
  // }

}
