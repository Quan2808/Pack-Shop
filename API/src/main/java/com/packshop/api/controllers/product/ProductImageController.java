package com.packshop.api.controllers.product;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.packshop.api.entities.product.ProductImage;
import com.packshop.api.services.product.ProductImageService;

@RestController
@RequestMapping("/api/product-images")
public class ProductImageController {

    @Autowired
    private ProductImageService productImageService;

    @GetMapping
    public List<ProductImage> getAllProductImages() {
        return productImageService.getAllProductImages();
    }   

    @GetMapping("/{id}")
    public ResponseEntity<ProductImage> getProductImageById(@PathVariable Long id) {
        Optional<ProductImage> productImage = productImageService.getProductImageById(id);
        return productImage.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductImage> createProductImage(@RequestBody ProductImage productImage) {
        ProductImage createdProductImage = productImageService.createProductImage(productImage);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProductImage);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductImage> updateProductImage(@PathVariable Long id, @RequestBody ProductImage productImage) {
        ProductImage updatedProductImage = productImageService.updateProductImage(id, productImage);
        return updatedProductImage != null ? ResponseEntity.ok(updatedProductImage) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductImage(@PathVariable Long id) {
        productImageService.deleteProductImage(id);
        return ResponseEntity.noContent().build();
    }
}
