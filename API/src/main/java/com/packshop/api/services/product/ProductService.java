package com.packshop.api.services.product;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.packshop.api.entities.product.Product;
import com.packshop.api.entities.product.ProductAttribute;
import com.packshop.api.repositories.product.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public ResponseEntity<Product> getProductById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<Product> createProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    public ResponseEntity<Product> updateProduct(Long id, Product product) {
        if (!productRepository.existsById(id))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Product existingProduct = productRepository.findById(id).get();

        existingProduct.setName(product.getName());
        existingProduct.setThumbnail(product.getThumbnail());
        existingProduct.setStatus(product.getStatus());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setMedia(product.getMedia());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setSku(product.getSku());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setCategory(product.getCategory());

        if (product.getProductAttribute() != null) {
            ProductAttribute existingAttribute = existingProduct.getProductAttribute();
            existingAttribute.setMaterial(product.getProductAttribute().getMaterial());
            existingAttribute.setDimensions(product.getProductAttribute().getDimensions());
            existingAttribute.setCapacity(product.getProductAttribute().getCapacity());
            existingAttribute.setWeight(product.getProductAttribute().getWeight());
        }

        Product updatedProduct = productRepository.save(existingProduct);

        return ResponseEntity.ok(updatedProduct);
    }

    public ResponseEntity<Void> deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
