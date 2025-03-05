package com.packshop.api.modules.shopping.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductItemDTO {
    private Long id;
    private String name;
    private Long price;
    private String thumbnail;
}
