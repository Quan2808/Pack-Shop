package com.packshop.api.modules.shopping.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductItemDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private String thumbnail;
}
