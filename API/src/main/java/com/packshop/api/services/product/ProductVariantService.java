package com.packshop.api.services.product;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.packshop.api.entities.product.ProductVariant;
import com.packshop.api.repositories.product.ProductVariantRepository;

@Service
public class ProductVariantService {

    @Autowired
    private ProductVariantRepository productVariantRepository;

    public List<ProductVariant> getAllProductVariants() {
        return productVariantRepository.findAll();
    }

    public Optional<ProductVariant> getProductVariantById(Long id) {
        return productVariantRepository.findById(id);
    }

    public ProductVariant saveProductVariant(ProductVariant productVariant) {
        return productVariantRepository.save(productVariant);
    }

    public void deleteProductVariant(Long id) {
        productVariantRepository.deleteById(id);
    }
}
