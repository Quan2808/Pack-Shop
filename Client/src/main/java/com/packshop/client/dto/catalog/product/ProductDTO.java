package com.packshop.client.dto.catalog.product;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String thumbnail;
    private String status;
    private String description;
    private List<String> media;
    private BigDecimal price;
    private String sku;
    private int quantity;
    private Long categoryId;
    private ProductAttributeDTO productAttribute;
}
