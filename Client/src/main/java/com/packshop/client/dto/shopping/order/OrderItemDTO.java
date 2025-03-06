package com.packshop.client.dto.shopping.order;

import com.packshop.client.dto.shopping.ProductItemDTO;

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