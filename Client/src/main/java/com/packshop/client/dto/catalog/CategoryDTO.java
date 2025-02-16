package com.packshop.client.dto.catalog;

import java.util.List;
import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private String image;
    private List<Long> productIds;
}
