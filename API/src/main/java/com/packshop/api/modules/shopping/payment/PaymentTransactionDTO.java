package com.packshop.api.modules.shopping.payment;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransactionDTO {
  private Long id;
  private Long orderId; // Chỉ cần orderId thay vì toàn bộ Order
  private String transactionId; // MoMo Transaction ID (TID)
  private String requestId; // Request ID gửi đến MoMo
  private String partnerCode; // Mã đối tác từ MoMo
  private Long amount; // Số tiền giao dịch
  private String status; // Trạng thái giao dịch (dùng String thay vì enum trực tiếp)
  private String payUrl; // URL thanh toán từ MoMo
  private String qrCodeUrl; // URL mã QR từ MoMo (nếu có)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'HH:mm")
  private LocalDateTime transactionDate; // Thời gian giao dịch
  private String responseMessage; // Thông điệp phản hồi từ MoMo
}
