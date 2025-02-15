package com.packshop.client.dto.catalog.product;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private MultipartFile thumbnailFile;
    private String thumbnail;
    private String status;
    private String description;
    private List<MultipartFile> mediaFiles;
    private List<String> media;
    private BigDecimal price;
    private String sku;
    private int quantity;
    private Long categoryId;
    private String material;
    private String dimensions;
    private String capacity;
    private String weight;
}