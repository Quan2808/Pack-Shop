package com.packshop.api.modules.shopping.dto;

import java.util.Set;

import lombok.Data;

@Data
public class CartDTO {
    private Long id;
    private int totalItems;
    private Set<CartItemDTO> cartItems;
}