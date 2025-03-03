package com.packshop.client.dto.shopping.cart;

import com.packshop.client.dto.shopping.ProductItemDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long id;
    private ProductItemDTO product;
    private int quantity;
}