package com.packshop.client.dto.catalog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;

    @NotBlank(message = "Product name cannot be empty")
    private String name;

    private String thumbnail;

    @NotBlank(message = "Status cannot be empty")
    private String status;

    private String description;

    private List<String> media;

    @NotNull(message = "Price cannot be null")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    @NotBlank(message = "SKU cannot be empty")
    private String sku;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 0, message = "Quantity must be greater than or equal to 0")
    private Integer quantity;

    @NotNull(message = "Category cannot be null")
    private Long categoryId;

    @NotNull(message = "Material cannot be null")
    @Size(min = 2, max = 50, message = "Material must be between 2 and 50 characters")
    private String material;

    @NotNull(message = "Dimensions cannot be null")
    @Size(min = 2, max = 100, message = "Dimensions must be between 2 and 100 characters")
    private String dimensions;

    @NotNull(message = "Capacity cannot be null")
    @Size(min = 2, max = 50, message = "Capacity must be between 2 and 50 characters")
    private String capacity;

    @NotNull(message = "Weight cannot be null")
    @Size(min = 2, max = 50, message = "Weight must be between 2 and 50 characters")
    private String weight;

    // Add fields for file upload
    private MultipartFile thumbnailFile;
    private List<MultipartFile> mediaFiles;
}
