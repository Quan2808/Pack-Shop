package com.packshop.client.dto.shopping.cart;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Long id;
    private int totalItems;
    private List<CartItemDTO> cartItems;
}