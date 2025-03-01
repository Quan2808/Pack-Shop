package com.packshop.api.modules.shopping.dto;

import lombok.Data;

@Data
public class CartItemDTO {
    private Long id;
    private Long product;
    private int quantity;
}