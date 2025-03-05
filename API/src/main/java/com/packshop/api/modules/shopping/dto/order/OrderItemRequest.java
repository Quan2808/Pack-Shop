package com.packshop.api.modules.shopping.dto.order;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long productId;
    private int quantity;
}
