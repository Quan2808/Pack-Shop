package com.packshop.api.services.catalog.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.packshop.api.entities.catalog.product.ProductAttribute;
import com.packshop.api.repositories.catalog.product.ProductAttributeRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ProductAttributeService {

    @Autowired
    private ProductAttributeRepository productAttributeRepository;

    public List<ProductAttribute> getAllProductAttributes() {
        return productAttributeRepository.findAll();
    }

    public ResponseEntity<ProductAttribute> getProductAttributeById(Long id) {
        Optional<ProductAttribute> productAttribute = productAttributeRepository.findById(id);
        return productAttribute.map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<ProductAttribute> createProductAttribute(ProductAttribute productAttribute) {
        ProductAttribute savedProductAttribute = productAttributeRepository.save(productAttribute);
        return new ResponseEntity<>(savedProductAttribute, HttpStatus.CREATED);
    }

    public ResponseEntity<ProductAttribute> updateProductAttribute(Long id, ProductAttribute productAttribute) {
        if (!productAttributeRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        productAttribute.setId(id);
        ProductAttribute updatedProductAttribute = productAttributeRepository.save(productAttribute);
        return ResponseEntity.ok(updatedProductAttribute);
    }

    public ResponseEntity<Void> deleteProductAttribute(Long id) {
        if (!productAttributeRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        productAttributeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
