package com.packshop.client.dto.shopping.address;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
  private Long id;

  @Size(max = 100, message = "Alias name must not exceed 100 characters")
  private String aliasName;

  @NotBlank(message = "Street address is required")
  @Size(max = 255, message = "Street address must not exceed 255 characters")
  private String streetAddress;

  @NotBlank(message = "Ward is required")
  @Size(max = 100, message = "Ward must not exceed 100 characters")
  private String ward;

  @NotBlank(message = "District is required")
  @Size(max = 100, message = "District must not exceed 100 characters")
  private String district;

  @NotBlank(message = "Province is required")
  @Size(max = 100, message = "Province must not exceed 100 characters")
  private String province;

  @Nullable
  private String fullAddress;

  @Builder.Default
  private Boolean isDefault = false;

}
