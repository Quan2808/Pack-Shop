package com.packshop.api.modules.shopping.payment.dto;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {
  @NotBlank(message = "Return URL is required")
  @URL(message = "Return URL must be a valid URL")
  private String returnUrl;

  @NotBlank(message = "Cancel URL is required")
  @URL(message = "Cancel URL must be a valid URL")
  private String cancelUrl;
}