package com.packshop.api.dto.catalog;

import java.math.BigDecimal;
import java.util.List;

import com.packshop.api.entities.catalog.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String thumbnail;
    private ProductStatus status;
    private String description;
    private List<String> media;
    private BigDecimal price;
    private String sku;
    private int quantity;
    private Long categoryId;
    // Product Attribute fields
    private String material;
    private String dimensions;
    private String capacity;
    private String weight;
}
