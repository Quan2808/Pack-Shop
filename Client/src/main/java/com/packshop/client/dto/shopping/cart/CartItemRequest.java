package com.packshop.client.dto.shopping.cart;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {
    // @NotNull(message = "Item ID is required")
    private Long id;

    // @NotNull(message = "Product ID is required")
    private Long productId;

    // @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than zero")
    private Integer quantity;
}