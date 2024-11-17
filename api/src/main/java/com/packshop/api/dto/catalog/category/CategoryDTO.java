package com.packshop.api.dto.catalog.category;

import java.util.List;

import com.packshop.api.dto.catalog.product.ProductDTO;

import lombok.Data;

@Data
public class CategoryDTO {

    private Long id;
    private String name;
    private String image;
    private List<ProductDTO> products;
}
