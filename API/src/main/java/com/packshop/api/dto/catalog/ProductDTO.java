package com.packshop.api.dto.catalog;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

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
    private ProductAttributeDTO attributes;
}
