package com.packshop.api.modules.shopping.dto;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Long id;
    private int totalItems;
    private Set<CartItemDTO> cartItems = new HashSet<>();
}