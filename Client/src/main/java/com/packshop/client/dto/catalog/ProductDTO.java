package com.packshop.client.dto.catalog;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private String material;
    private String dimensions;
    private String capacity;
    private String weight;

    // Add fields for file upload
    private MultipartFile thumbnailFile;
    private List<MultipartFile> mediaFiles;
}