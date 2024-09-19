package com.packshop.api.services.product;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.packshop.api.entities.product.ProductImage;
import com.packshop.api.repositories.product.ProductImageRepository;

@Service
public class ProductImageService {

    @Autowired
    private ProductImageRepository productImageRepository;

    public List<ProductImage> getAllProductImages() {
        return productImageRepository.findAll();
    }

    public Optional<ProductImage> getProductImageById(Long id) {
        return productImageRepository.findById(id);
    }

    public ProductImage createProductImage(ProductImage productImage) {
        return productImageRepository.save(productImage);
    }

    public ProductImage updateProductImage(Long id, ProductImage productImage) {
        if (productImageRepository.existsById(id)) {
            productImage.setId(id);
            return productImageRepository.save(productImage);
        }
        return null;
    }

    public void deleteProductImage(Long id) {
        productImageRepository.deleteById(id);
    }
}
