package com.packshop.client.dto.catalog;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.DecimalMin;
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
    private String status;
    private String description;

    private List<String> media;

    @NotNull(message = "Price cannot be null")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    @NotBlank(message = "SKU cannot be empty")
    private String sku;

    @Min(value = 0, message = "Quantity must be greater than or equal to 0")
    @NotNull(message = "Quantity cannot be null")
    private Integer quantity;

    @NotNull(message = "Category cannot be null")
    private Long categoryId;

    @Size(min = 2, max = 50, message = "Material should be between 2 and 50 characters")
    @NotNull(message = "Material cannot be null")
    private String material;

    @Size(min = 2, max = 100, message = "Dimensions should be between 2 and 100 characters")
    @NotNull(message = "Dimensions cannot be null")
    private String dimensions;

    @Size(min = 2, max = 50, message = "Capacity should be between 2 and 50 characters")
    @NotNull(message = "Capacity cannot be null")
    private String capacity;

    @DecimalMin(value = "0.0", inclusive = true, message = "Weight must be greater than or equal to 0")
    @NotNull(message = "Weight cannot be null")
    private String weight;

    // Add fields for file upload
    private MultipartFile thumbnailFile;
    private List<MultipartFile> mediaFiles;
}
