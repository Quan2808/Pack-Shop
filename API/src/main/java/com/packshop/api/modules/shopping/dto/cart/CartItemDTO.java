package com.packshop.api.modules.shopping.dto.cart;

import com.packshop.api.modules.shopping.dto.ProductItemDTO;

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