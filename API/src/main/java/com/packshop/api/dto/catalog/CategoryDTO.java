package com.packshop.api.dto.catalog;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CategoryDTO {

    private Long id;
    private String name;
    private String image;
    private List<ProductDTO> products;
}

