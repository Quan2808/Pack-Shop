package com.packshop.api.modules.shopping.payment;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  @JsonBackReference
  private Order order;

  @Column(name = "transaction_id", nullable = false)
  private String transactionId; // MoMo Transaction ID (TID)

  @Column(nullable = false)
  private String requestId; // Request ID gửi đến MoMo

  @Column(nullable = false)
  private String partnerCode; // Mã đối tác từ MoMo

  @Column(nullable = false)
  private Long amount; // Số tiền giao dịch

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status; // Trạng thái giao dịch

  @Column
  private String payUrl; // URL thanh toán từ MoMo

  @Column
  private String qrCodeUrl; // URL mã QR từ MoMo (nếu có)

  @Column
  private LocalDateTime transactionDate; // Thời gian giao dịch

  @Column
  private String responseMessage; // Thông điệp phản hồi từ MoMo

  public enum PaymentStatus {
    PENDING, // Chờ xử lý
    SUCCESS, // Thành công
    FAILED, // Thất bại
    CANCELLED // Hủy bỏ
  }
}