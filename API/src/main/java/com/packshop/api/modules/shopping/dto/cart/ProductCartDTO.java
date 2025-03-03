package com.packshop.api.modules.shopping.dto.cart;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCartDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private String thumbnail;
}
