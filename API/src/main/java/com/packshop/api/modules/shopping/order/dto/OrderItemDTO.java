package com.packshop.api.modules.shopping.order.dto;

import com.packshop.api.modules.shopping.dto.ProductItemDTO;

import lombok.Data;

@Data
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Long unitPrice;
    private int quantity;
    private Long subtotal;
    private ProductItemDTO product;
}