package com.packshop.api.controllers.product;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.packshop.api.entities.product.ProductVariant;
import com.packshop.api.services.product.ProductVariantService;

@RestController
@RequestMapping("/api/product-variants")
public class ProductVariantController {

    @Autowired
    private ProductVariantService productVariantService;

    @GetMapping
    public List<ProductVariant> getAllProductVariants() {
        return productVariantService.getAllProductVariants();
    }

    @GetMapping("/{id}")
    public Optional<ProductVariant> getProductVariantById(@PathVariable Long id) {
        return productVariantService.getProductVariantById(id);
    }

    @PostMapping
    public ProductVariant createProductVariant(@RequestBody ProductVariant productVariant) {
        return productVariantService.saveProductVariant(productVariant);
    }

    @PutMapping("/{id}")
    public ProductVariant updateProductVariant(@PathVariable Long id, @RequestBody ProductVariant productVariant) {
        productVariant.setId(id);
        return productVariantService.saveProductVariant(productVariant);
    }

    @DeleteMapping("/{id}")
    public void deleteProductVariant(@PathVariable Long id) {
        productVariantService.deleteProductVariant(id);
    }
}
