package com.packshop.client.dto.catalog.product;

import lombok.Data;

@Data
public class ProductAttributeDTO {

    private Long id;
    private String material;
    private String dimensions;
    private String capacity;
    private String weight;
}
